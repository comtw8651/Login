package com.example.demo.entity;

/**
 * ✅ QuizResultSummary：封裝某部影片的測驗統計總結資訊
 * 通常用於顯示「某影片累積測驗的答對情形」，對應前端 quiz summary 頁面使用
 */
public class QuizResultSummary {

    private String videoId;          // ✅ 影片 ID（資料表 video 的主鍵）
    private String videoTitle;      // ✅ 影片標題（額外補充欄位，用於顯示用）
    private String source;          // ✅ 題目來源（如 GPT、自動產生、本地題庫）
    private int totalQuizCount;     // ✅ 該來源下總共測驗幾次
    private int total;              // ✅ 該來源下總共做過幾題
    private int correct;            // ✅ 總共答對幾題

    // ✅ 建構子（用於查詢結果封裝）
    public QuizResultSummary(String videoId, String source, int totalQuizCount, int total, int correct) {
        this.videoId = videoId;
        this.source = source;
        this.totalQuizCount = totalQuizCount;
        this.total = total;
        this.correct = correct;
    }

    // ✅ 無參數建構子（保留給 Spring JDBC 或 Gson 用）
    public QuizResultSummary() {}

    // --- 以下為標準 Getter / Setter 方法 ---

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getTotalQuizCount() {
        return totalQuizCount;
    }

    public void setTotalQuizCount(int totalQuizCount) {
        this.totalQuizCount = totalQuizCount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }
}
