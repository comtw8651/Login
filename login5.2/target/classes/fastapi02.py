from fastapi import FastAPI, Request, UploadFile, File, Form, HTTPException
from fastapi.responses import StreamingResponse, HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import os
import tempfile
import subprocess
import asyncio
import torch
import whisper
import yt_dlp
import srt

# -------- 目錄＆路徑設定 --------
# 專案根目錄
BASE_DIR = Path(__file__).resolve().parent

# 模板目錄（對應 login3.6/src/main/resources/templates）
TEMPLATE_DIR = BASE_DIR / "login3.6" / "src" / "main" / "resources" / "templates"
# 靜態資源目錄（若有 css/js/img 放在此）
STATIC_DIR   = BASE_DIR / "login3.6" / "src" / "main" / "resources" / "static"

# -------- 應用初始化 --------
app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],    # 若前端同源就可限制為你的網域
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 設定模板引擎
templates = Jinja2Templates(directory=str(TEMPLATE_DIR))

# 掛載靜態資源（不存在就不掛載）
if STATIC_DIR.exists():
    app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

# -------- Whisper 模型＆線程池 --------
# FFMPEG 可執行檔路徑，請改成你本機安裝的位置
FFMPEG_PATH = r"C:\Users\ysmso\Downloads\ffmpeg-7.1.1-essentials_build\bin"

# 建立線程池，用來並行處理音訊分段
executor = ThreadPoolExecutor(max_workers=4)

# 檢查 CUDA 是否可用
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
print(f"使用裝置：{DEVICE}")
if DEVICE == "cuda":
    print(f"GPU 型號：{torch.cuda.get_device_name(0)}")

# 載入 Whisper 模型
try:
    model = whisper.load_model("base").to(DEVICE)
    print("Whisper 模型載入完成")
except Exception as e:
    raise RuntimeError(f"模型載入失敗：{e}")

# -------- 工具函式 --------
def get_audio_duration(audio_path: str) -> float:
    """用 ffprobe 取得音訊長度（秒）"""
    result = subprocess.run([
        os.path.join(FFMPEG_PATH, "ffprobe"),
        "-v", "error",
        "-show_entries", "format=duration",
        "-of", "default=noprint_wrappers=1:nokey=1",
        audio_path
    ], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
    return float(result.stdout.decode().strip())

def split_audio(audio_path: str, segment_duration: int = 15):
    """將音訊依照 segment_duration（秒）切割成多個小段"""
    output_dir = Path(audio_path).parent / "segments"
    output_dir.mkdir(exist_ok=True)
    total = get_audio_duration(audio_path)
    segments = []
    for start in range(0, int(total), segment_duration):
        seg_path = output_dir / f"seg_{start}.mp3"
        cmd = [
            os.path.join(FFMPEG_PATH, "ffmpeg"),
            "-i", audio_path,
            "-ss", str(start),
            "-t", str(segment_duration),
            "-acodec", "libmp3lame",
            "-q:a", "4",
            "-ar", "16000",
            str(seg_path), "-y"
        ]
        subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        segments.append((start, str(seg_path)))
    return segments

def format_time(seconds: float) -> str:
    """將秒數轉換為 SRT 格式時間：HH:MM:SS,mmm"""
    h = int(seconds // 3600)
    m = int((seconds % 3600) // 60)
    s = int(seconds % 60)
    ms = int((seconds - int(seconds)) * 1000)
    return f"{h:02d}:{m:02d}:{s:02d},{ms:03d}"

def process_audio_segment(segment_path: str, start_time: float = 0) -> str:
    """使用 Whisper 轉錄單一音訊分段，並回傳 SRT 文字"""
    res = model.transcribe(
        segment_path,
        verbose=False,
        task="transcribe",
        language="zh",
        fp16=(DEVICE == "cuda")
    )
    srt_text = ""
    idx = 1
    for seg in res["segments"]:
        st = seg["start"] + start_time
        et = seg["end"]   + start_time
        srt_text += f"{idx}\n"
        srt_text += f"{format_time(st)} --> {format_time(et)}\n"
        srt_text += f"{seg['text'].strip()}\n\n"
        idx += 1
    return srt_text

# -------- 路由：頁面渲染 --------
@app.get("/", response_class=HTMLResponse)
async def home(request: Request):
    """
    渲染 index.html（位於 login3.6/src/main/resources/templates/index.html）
    在模板中引用靜態資源範例：
      <link href="/static/css/main.css" rel="stylesheet">
      <script src="/static/js/chat.js"></script>
    """
    return templates.TemplateResponse("index.html", {"request": request})

# -------- 路由：字幕生成功能 API --------
@app.post("/api/generate-subtitle")
async def generate_subtitle(
    file: UploadFile = File(None),
    youtube_url: str    = Form(None),
    segment_duration: int = Form(15)
):
    if not file and not youtube_url:
        raise HTTPException(status_code=400, detail="請上傳音訊檔或提供 YouTube 連結")

    async def streamer():
        with tempfile.TemporaryDirectory() as tmp:
            audio_path = None

            # 若提供 YouTube 連結，先下載音訊
            if youtube_url:
                opts = {
                    "format": "bestaudio",
                    "outtmpl": os.path.join(tmp, "audio.%(ext)s"),
                    "postprocessors": [{
                        "key": "FFmpegExtractAudio",
                        "preferredcodec": "mp3",
                        "preferredquality": "128"
                    }],
                    "ffmpeg_location": FFMPEG_PATH
                }
                with yt_dlp.YoutubeDL(opts) as ydl:
                    ydl.download([youtube_url])
                for f in os.listdir(tmp):
                    if f.endswith(".mp3"):
                        audio_path = os.path.join(tmp, f)
                        break

            # 若上傳檔案，存到暫存目錄
            if file:
                path = os.path.join(tmp, file.filename)
                with open(path, "wb") as out:
                    out.write(await file.read())
                audio_path = path

            if not audio_path:
                raise HTTPException(status_code=500, detail="音訊處理失敗")

            # 切割並依序轉錄，逐段回傳 SRT 文字
            segments = split_audio(audio_path, segment_duration)
            loop = asyncio.get_event_loop()
            for st, seg_path in segments:
                srt_chunk = await loop.run_in_executor(
                    executor, process_audio_segment, seg_path, st
                )
                yield srt_chunk

    return StreamingResponse(streamer(), media_type="text/plain")
