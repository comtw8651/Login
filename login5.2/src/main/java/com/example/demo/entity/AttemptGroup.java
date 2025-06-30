package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ AttemptGroup：表示「一次測驗紀錄」
 *
 * 此類別代表同一個 `attemptId` 的作答紀錄集合，
 * 對應一次完整的作答行為，會包含：
 *   - 所有題目詳解（AnswerRecord 列表）
 *   - 提交時間、來源、正確率等統計資訊
 *
 * 📌 用於 quizHistory 詳解頁（例如 quizhistory.html）或結果統計後端 API。
 */
public class AttemptGroup {

    private long attemptId;                // 🧾 測驗批次編號（唯一值，每次作答自動產生）
    private LocalDateTime submittedAt;    // 🕒 本次測驗提交時間
    private String source;                // 🧠 題目來源（GPT 或 local）
    private int total;                    // 🧮 題目總數
    private int correct;                  // ✅ 答對題數
    private double accuracy;              // 📊 正確率（例：0.6 表示 60%）

    private List<AnswerRecord> questions; // 📋 本次作答的所有題目詳解

    // --- Getter / Setter 區 ---

    public long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(long attemptId) {
        this.attemptId = attemptId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public List<AnswerRecord> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AnswerRecord> questions) {
        this.questions = questions;
    }
}
