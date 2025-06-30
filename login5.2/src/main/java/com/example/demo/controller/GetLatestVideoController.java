package com.example.demo.controller;

// 匯入 JSON 物件處理類別（org.json）
import org.json.JSONObject;

// 匯入 Spring Boot 相關註解與類別
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與模型
import com.example.demo.dao.VideoDAO;
import com.example.demo.entity.Video;

/**
 * GetLatestVideoController：取得最新上架影片的資訊
 * 路徑：GET /api/getLatestVideo
 * 回傳範例 JSON：{ "id": 1, "title": "Java 基礎入門", "videoId": "YouTubeID" }
 */
@RestController // 標示為 REST 控制器，回傳 JSON 字串
@RequestMapping("/api") // 所有 API 路徑皆以 /api 開頭
public class GetLatestVideoController {

    @Autowired // 由 Spring 自動注入 VideoDAO 實例
    private VideoDAO videoDAO;

    /**
     * 取得資料庫中最新一筆影片記錄
     * @return JSON 格式字串，包含影片 id、標題與 videoId（為純 YouTube ID）
     */
    @GetMapping("/getLatestVideo")
    public String getLatestVideo() {
        try {
            // 從資料庫取得最新影片物件
            Video video = videoDAO.getLatestVideo();

            if (video == null) {
                // 若找不到影片則回傳錯誤訊息
                return "{\"error\":\"找不到影片\"}";
            }

            // 擷取乾淨的 YouTube videoId（去除網址多餘參數）
            String cleanVideoId = extractYouTubeId(String.valueOf(video.getId()));

            // 建立 JSON 物件並加入欄位
            JSONObject json = new JSONObject();
            json.put("id", video.getId());
            json.put("title", video.getTitle());
            json.put("videoId", cleanVideoId);

            // 回傳 JSON 字串
            return json.toString();

        } catch (Exception e) {
            // 例外處理：回傳錯誤訊息（替換掉雙引號避免 JSON 格式錯誤）
            e.printStackTrace();
            return "{\"error\":\"後端錯誤：" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    /**
     * 萃取 YouTube 網址中的純 videoId（支援多種格式）
     * @param url 原始影片網址（可能為完整網址、短網址或嵌入碼）
     * @return 純 videoId 字串（或錯誤提示字串）
     */
    private String extractYouTubeId(String url) {
        if (url == null || url.isEmpty()) return "";

        // 解析格式一：https://www.youtube.com/watch?v=XXXXXX
        if (url.contains("youtube.com/watch?v=")) {
            String temp = url.substring(url.indexOf("watch?v=") + 8);
            int ampIndex = temp.indexOf("&");
            return (ampIndex != -1) ? temp.substring(0, ampIndex) : temp;
        }

        // 解析格式二：https://youtu.be/XXXXXX
        if (url.contains("youtu.be/")) {
            String temp = url.substring(url.indexOf("youtu.be/") + 9);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // 解析格式三：https://www.youtube.com/embed/XXXXXX
        if (url.contains("embed/")) {
            String temp = url.substring(url.indexOf("embed/") + 6);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // 如果是 playlist 連結，視為無效
        if (url.contains("list=")) {
            return "INVALID_PLAYLIST_ID";
        }

        // 其他格式，直接回傳原始值（可能是 ID）
        return url;
    }
}
