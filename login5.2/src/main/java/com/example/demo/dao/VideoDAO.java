package com.example.demo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Video;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * VideoDAO：處理影片 video 表相關資料存取邏輯
 */
@Repository
public class VideoDAO {

    private final JdbcTemplate jdbcTemplate;

    public VideoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * ✅ 影片資料表對應的 RowMapper（資料庫欄位對應到 Video 物件屬性）
     * 用於 JdbcTemplate 查詢結果轉換為 Video 物件
     */
    private static final RowMapper<Video> videoRowMapper = (rs, rowNum) -> {
        Video v = new Video();

        // ✅ 主鍵 ID：現在是 Long 型別
        // 使用 getLong() 獲取，如果資料庫欄位是 NULL，getLong() 會返回 0，這可能需要額外處理
        v.setId(rs.getLong("id"));

        // ✅ YouTube 影片 ID：對應到 Entity 的 youtubeId 屬性
        // 假設你的資料庫欄位名稱仍然是 "video_id"
        v.setYoutubeId(rs.getString("video_id"));

        // ✅ 影片標題
        v.setTitle(rs.getString("title"));

        // ✅ 影片描述
        v.setDescription(rs.getString("description"));

        // ✅ 影片播放網址：新 Entity 欄位，從資料庫中取得
        v.setVideoUrl(rs.getString("video_url"));

        // ✅ 縮圖網址
        v.setThumbnailUrl(rs.getString("thumbnail_url"));

        // ✅ 發佈日期：從 String 轉換為 LocalDateTime
        String publishedAtString = rs.getString("published_at");
        if (publishedAtString != null && !publishedAtString.trim().isEmpty()) {
            try {
                // 預設解析 ISO 8601 格式 (例如 "2024-06-27T14:30:00")
                v.setPublishedAt(LocalDateTime.parse(publishedAtString));
            } catch (DateTimeParseException e) {
                System.err.println("Warning: Failed to parse published_at string '" + publishedAtString + "'. Setting to null. Error: " + e.getMessage());
                v.setPublishedAt(null); // 解析失敗時設為 null
            }
        } else {
            v.setPublishedAt(null); // 資料庫值為 null 或空字串時設為 null
        }

        // ✅ 影片類型：新 Entity 欄位，從資料庫中取得
        v.setType(rs.getString("type"));

        // ✅ 影片主題：新 Entity 欄位，從資料庫中取得
        v.setSubject(rs.getString("subject"));

        // ✅ 影片總長度（秒）：新 Entity 欄位，使用 getObject() 並轉型以處理 NULL 值
        v.setDurationSeconds((Integer) rs.getObject("duration_seconds"));

        // ✅ 影片記錄的創建時間：新 Entity 欄位，從 String 轉換為 LocalDateTime
        String createdAtString = rs.getString("created_at");
        if (createdAtString != null && !createdAtString.trim().isEmpty()) {
            try {
                v.setCreatedAt(LocalDateTime.parse(createdAtString));
            } catch (DateTimeParseException e) {
                System.err.println("Warning: Failed to parse created_at string '" + createdAtString + "'. Setting to current time. Error: " + e.getMessage());
                v.setCreatedAt(LocalDateTime.now()); // 解析失敗時設為當前時間
            }
        } else {
            v.setCreatedAt(LocalDateTime.now()); // 資料庫值為 null 或空字串時設為當前時間
        }

        // `chatSessions` 是 OneToMany 關聯，不會在 RowMapper 中直接映射。
        // JPA 會處理這個列表的載入。

        return v;
    };

    /**
     * ✅ 新增單一影片記錄至 video 表
     *
     * @param v Video 物件，包含影片基本資料
     */
    public void insertVideo(Video v) {
        String sql = "INSERT INTO video (video_id, title, description, thumbnail_url, published_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, v.getId(), v.getTitle(), v.getDescription(), v.getThumbnailUrl(), v.getPublishedAt());
    }

    /**
     * ✅ 批次新增影片資料（逐筆插入）
     *
     * @param videos List<Video> 多筆影片資料
     * 注意：本方法會逐筆呼叫 insertVideo，非使用 batch insert
     */
    public void insertVideoList(List<Video> videos) {
        for (Video v : videos) {
            insertVideo(v);
        }
    }

    /**
     * ✅ 根據主鍵 id 查詢單一影片記錄
     *
     * @param id video 表中的主鍵欄位（非 YouTube videoId）
     * @return 對應的 Video 物件，若找不到則回傳 null
     */
    public Video getVideoById(String id) {
        String sql = "SELECT * FROM video WHERE id = ?";
        return jdbcTemplate.query(sql, videoRowMapper, id).stream().findFirst().orElse(null);
    }

    /**
     * ✅ 查詢所有影片，並依照標題長度與字母排序
     *
     * @return List<Video> 全部影片清單
     * 排序依據：標題長度（越短越前）→ 再按字母排序
     */
    public List<Video> getAllVideos() {
        String sql = "SELECT * FROM video ORDER BY LENGTH(title), title";
        return jdbcTemplate.query(sql, videoRowMapper);
    }

    /**
     * ✅ 查詢影片的 YouTube 原始連結網址（若欄位有存）
     *
     * @param id 主鍵 ID
     * @return 對應的 youtube_url 字串，若無則為 null
     */
    public String getYoutubeUrl(String id) {
        String sql = "SELECT youtube_url FROM video WHERE id = ?";
        return jdbcTemplate.query(sql, (rs) -> rs.next() ? rs.getString("youtube_url") : null, id);
    }

    /**
     * ✅ 查詢最新加入的影片（依照 ID 倒序排列）
     *
     * @return 最新一筆 Video 物件，若無則為 null
     */
    public Video getLatestVideo() {
        String sql = "SELECT * FROM video ORDER BY id DESC LIMIT 1";
        return jdbcTemplate.query(sql, videoRowMapper).stream().findFirst().orElse(null);
    }

    /**
     * ✅ 依照指定欄位排序查詢所有影片
     *
     * @param sortBy 可傳入的排序參數有：title / published / videoId
     * @return 排序後的 List<Video>
     * 若 sortBy 不合法，預設以 title 排序
     */
    public List<Video> getAllVideosSorted(String sortBy) {
        // 判斷排序條件，避免 SQL Injection（用 switch 控制）
        String orderClause = switch (sortBy) {
            case "title" -> "ORDER BY title";
            case "published" -> "ORDER BY published_at DESC";
            case "videoId" -> "ORDER BY video_id";
            default -> "ORDER BY title";
        };
        String sql = "SELECT * FROM video " + orderClause;
        return jdbcTemplate.query(sql, videoRowMapper);
    }
}
