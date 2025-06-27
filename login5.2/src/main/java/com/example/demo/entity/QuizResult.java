package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_results") // 指定對應的資料庫表格名稱
public class QuizResult {

    @Id // 主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動增長ID
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "total_questions") // 總題數
    private Integer totalQuestions;

    @Column(name = "correct_answers") // 答對題數
    private Integer correctAnswers;

    @Column(precision = 5, scale = 2) // 準確度，例如 99.99
    private BigDecimal accuracy;

    @Column(name = "submitted_at", nullable = false) // 提交時間，不可為空
    private LocalDateTime submittedAt;

    @Column(length = 50, nullable = false) // 來源，預設為 "local"，長度限制50
    private String source = "local";

    @Column(name = "attempt_id", nullable = false) // 測驗批次編號，不可為空
    private Long attemptId;

    @Column(length = 20) // 新增來自 model 的 difficulty 屬性，長度限制20
    private String difficulty; // 題目的難易度（如 easy、medium、hard）

    // 在實體持久化到資料庫前執行的方法，用於自動設定 submittedAt
    @PrePersist
    protected void onSubmit() {
        submittedAt = LocalDateTime.now();
    }

    // --- 構造函數 (Constructor) ---
    public QuizResult() {
    }

    // --- Getter 和 Setter 方法 ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

    public Long getVideoId() {
		return videoId;
	}

	public void setVideoId(Long videoId) {
		this.videoId = videoId;
	}

	public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
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

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}