package com.example.demo.entity;

/**
 * ✅ AnswerRecord：用於顯示「每一題作答詳解」的資料模型
 *
 * 此類別主要是回傳單題的詳解資訊給前端，
 * 包含：題目內容、四個選項、正確答案索引與文字、使用者的選擇索引、解析等等。
 *
 * ⚙️ 常見用途：
 * - 測驗結果頁（result.html）
 * - 錯題回顧頁（quizhistory.html）
 * - 依作答 attemptId 整批回傳
 */
public class AnswerRecord {

    private Integer quizId;          // 🆔 題目 ID（quiz 資料表的主鍵）
    private Integer selectedIndex;   // ❌ 使用者所選擇的選項索引（0~3）
    private Integer answerIndex;     // ✅ 正確答案的選項索引（0~3）

    private String question;         // ❓ 題目內容
    private String option1;          // 選項 A
    private String option2;          // 選項 B
    private String option3;          // 選項 C
    private String option4;          // 選項 D

    private String source;           // 🧠 題目來源（如 GPT 或 local）
    private String answer;           // ✅ 正確答案的選項文字（例如 "影片中提到..."）
    private String explanation;      // 📘 題目解析說明

    private Long attemptId;          // 🧾 所屬作答批次 ID（用來分群）
    private String videoId;          // 🎬 對應影片 ID

    // --- Getter / Setter ---

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public Integer getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(Integer answerIndex) {
        this.answerIndex = answerIndex;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
