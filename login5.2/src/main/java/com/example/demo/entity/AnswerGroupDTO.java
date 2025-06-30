package com.example.demo.entity;

import java.util.List;

/**
 * ✅ AnswerGroupDTO：封裝「某部影片」的所有測驗紀錄資訊
 * 
 * 此類別是為了回傳「影片為單位的測驗歷史資料」給前端使用，
 * 每一部影片包含多次測驗紀錄（attempt），每次 attempt 又會有多題作答詳解。
 *
 * ⚙️ 常見使用場景：
 * - quizHistoryDetail 頁面（按影片群組顯示所有測驗詳解）
 * - 回傳結構為：
 *   {
 *     "videoId": "abc123",
 *     "videoTitle": "AI 簡介",
 *     "attempts": [ {...}, {...}, ... ]
 *   }
 */
public class AnswerGroupDTO {

    private String videoId;                  // 🎬 影片 ID（例如 YouTube videoId）
    private String videoTitle;               // 🎥 影片標題
    private List<AttemptGroup> attempts;     // 📊 多次測驗的分組，每次測驗一筆 AttemptGroup

    // --- Getter / Setter ---

    /**
     * 取得影片 ID
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * 設定影片 ID
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * 取得影片標題
     */
    public String getVideoTitle() {
        return videoTitle;
    }

    /**
     * 設定影片標題
     */
    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    /**
     * 取得所有測驗紀錄分組（每次測驗）
     */
    public List<AttemptGroup> getAttempts() {
        return attempts;
    }

    /**
     * 設定所有測驗紀錄分組
     */
    public void setAttempts(List<AttemptGroup> attempts) {
        this.attempts = attempts;
    }
}
