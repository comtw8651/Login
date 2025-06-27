from fastapi import FastAPI, Request, UploadFile, File, Form
from fastapi.responses import FileResponse, HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

import whisper
import subprocess
import os
from concurrent.futures import ThreadPoolExecutor

def get_audio_duration(filename: str) -> float:
    """
    获取音频时长（秒）。
    """
    result = subprocess.run(
        [
            "ffprobe", "-v", "error", "-show_entries",
            "format=duration", "-of",
            "default=noprint_wrappers=1:nokey=1", filename
        ], capture_output=True, text=True
    )
    return float(result.stdout.strip())

# 切分音频，返回切分后的文件路径列表

def split_audio(filename: str, segment_length: int = 15) -> list[str]:
    duration = get_audio_duration(filename)
    segments = []
    idx = 0
    start = 0.0
    while start < duration:
        end = min(start + segment_length, duration)
        out_file = f"segment_{idx}.wav"
        subprocess.run([
            "ffmpeg", "-y", "-i", filename,
            "-ss", str(start), "-to", str(end),
            "-acodec", "pcm_s16le", "-ac", "1", out_file
        ], capture_output=True)
        segments.append((idx, start, end, out_file))
        idx += 1
        start = end
    return segments

# 生成文本（TXT）内容

def transcribe_to_txt(model, segments_info: list[tuple[int, float, float, str]]) -> str:
    results = []
    def transcribe_segment(idx, start, end, wav_path):
        result = model.transcribe(wav_path)
        text = result.get('text', '').strip()
        return idx, start, end, text

    with ThreadPoolExecutor() as executor:
        futures = [executor.submit(transcribe_segment, idx, start, end, path)
                   for idx, start, end, path in segments_info]
        for future in futures:
            results.append(future.result())

    # 按原序排序，然后拼接
    results.sort(key=lambda x: x[0])
    txt_lines = []
    for idx, start, end, text in results:
        txt_lines.append(f"{idx+1}\n{text}\n")
    return "\n".join(txt_lines)

# 初始化
app = FastAPI()

# 跨域配置，如需静态资源可继续保留此部分
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Jinja2 模板目录，前端页面放在 templates/ 目录
templates = Jinja2Templates(directory="templates")

# 若有 CSS/JS 可以再挂载静态目录
app.mount("/static", StaticFiles(directory="static"), name="static")

# 首页渲染模板
@app.get("/", response_class=HTMLResponse)
async def index(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})

# 接收音频或 YouTube 链接，生成 TXT 并返回下载
@app.post("/api/generate-txt")
async def generate_txt(
    request: Request,
    file: UploadFile = File(None),
    url: str = Form(None)
) -> FileResponse:
    # 保存上传或下载的音频
    audio_path = "input_audio.wav"
    if file:
        with open(audio_path, "wb") as f:
            f.write(await file.read())
    elif url:
        # 使用 yt-dlp 下载音频
        subprocess.run([
            "yt-dlp", "-x", "--audio-format", "wav",
            "-o", audio_path, url
        ], capture_output=True)
    else:
        return {
            "error": "请提供上传文件或 YouTube 链接"
        }

    # 切分音频并转录
    segments = split_audio(audio_path)
    model = whisper.load_model("base")
    txt_content = transcribe_to_txt(model, segments)

    # 将 TXT 写入文件
    out_path = "subtitles.txt"
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(txt_content)

    # 删除临时切片
    for _, _, _, path in segments:
        os.remove(path)

    # 返回文件下载
    return FileResponse(
        path=out_path,
        filename="subtitles.txt",
        media_type="text/plain"
    )
