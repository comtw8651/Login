package com.example.demo.controller;

// 匯入 JSON 組裝工具
import org.json.JSONObject;

// 匯入 Spring Boot 控制器與注入相關套件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與影片模型類別
import com.example.demo.dao.VideoDAO;
import com.example.demo.entity.Video;

/**
 * GetVideoController：根據主鍵 ID 查詢特定影片資訊
 * API 路徑：GET /api/getVideo?videoId=1
 * 回傳格式範例：
 * {
 *   "id": 1,
 *   "title": "Java 入門教學",
 *   "videoId": "YouTubeID"
 * }
 */
@RestController // 標示為 REST 控制器，回傳純 JSON 格式資料
@RequestMapping("/api") // 所有 API 路徑都以 /api 開頭
public class GetVideoController {

    @Autowired // 由 Spring 自動注入 VideoDAO 實作
    private VideoDAO videoDAO;

    /**
     * 根據主鍵 ID 查詢資料庫影片資料
     * @param videoId 整數型的影片主鍵 ID（非 YouTube ID）
     * @return 回傳影片資料的 JSON 字串，或錯誤訊息
     */
    @GetMapping("/getVideo")
    public String getVideoById(@RequestParam("videoId") int videoId) {
        try {
            // 呼叫 DAO 查詢影片，注意轉為 String 傳入
            Video video = videoDAO.getVideoById(String.valueOf(videoId)); // 注意：DAO 設計接受 String 型別

            if (video == null) {
                // 若查無資料，回傳錯誤 JSON
                return "{\"error\": \"找不到影片\"}";
            }

            // 擷取純粹的 YouTube videoId（去除網址參數）
            String cleanVideoId = extractYouTubeId(video.getVideoId());

            // 將資料轉為 JSON 回傳
            JSONObject json = new JSONObject();
            json.put("id", video.getId());
            json.put("title", video.getTitle());
            json.put("videoId", cleanVideoId);

            return json.toString(); // ✅ 回傳 JSON 字串

        } catch (Exception e) {
            // 發生例外時的錯誤回傳處理
            e.printStackTrace();
            return "{\"error\":\"後端錯誤：" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    /**
     * 擷取純 YouTube videoId（支援多種網址格式）
     * @param url 使用者或資料庫存入的原始 YouTube 網址
     * @return 純 videoId（不含參數）
     */
    private String extractYouTubeId(String url) {
        if (url == null || url.isEmpty()) return "";

        // 支援格式 1：https://www.youtube.com/watch?v=XXXXXX
        if (url.contains("youtube.com/watch?v=")) {
            String temp = url.substring(url.indexOf("watch?v=") + 8);
            int ampIndex = temp.indexOf("&");
            return (ampIndex != -1) ? temp.substring(0, ampIndex) : temp;
        }

        // 支援格式 2：https://youtu.be/XXXXXX
        if (url.contains("youtu.be/")) {
            String temp = url.substring(url.indexOf("youtu.be/") + 9);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // 支援格式 3：https://www.youtube.com/embed/XXXXXX
        if (url.contains("embed/")) {
            String temp = url.substring(url.indexOf("embed/") + 6);
            int paramIndex = temp.indexOf("?");
            return (paramIndex != -1) ? temp.substring(0, paramIndex) : temp;
        }

        // playlist 類型不支援播放（視為錯誤）
        if (url.contains("list=")) {
            return "INVALID_PLAYLIST_ID";
        }

        // 若以上皆不符，視為已是純 ID，直接回傳
        return url;
    }
}
