package com.example.demo.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient; // 引入 Transient 註解

/**
 * ✅ Answer：使用者作答紀錄的資料模型，對應資料表 `answer`。
 * 代表單一作答記錄，包含使用者、題目、影片、選擇的選項、作答結果、時間戳記及題目內容詳情。
 */
@Entity // 標記為 JPA 實體類別
@Table(name = "answer") // 對應資料庫中的 `answer` 表
public class Answer {

    // ✅ 作答記錄主鍵 ID，自動編號
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "quiz_id")
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "video_id")
    private Long videoId;

    // ✅ 使用者選擇的選項索引（0~3）
    @Column(name = "selected_option")
    private Integer selectedOption;

    // ✅ 是否答對
    @Column(name = "is_correct")
    private Boolean isCorrect;

    // ✅ 資料建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ 使用者作答時間
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    // ✅ 題目來源（如 gpt/local）
    @Column(length = 10) // 限制字串長度
    private String source = "local"; // 提供預設值 "local"

    // ✅ 作答批次 ID（一次測驗共用），例如同一個測驗或同一批題目
    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    // ✅ 提交整份測驗的時間
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // ✅ 題目文字 (從第二個檔案引入，方便直接記錄題目內容)
    @Column(columnDefinition = "TEXT")
    private String question;

    // ✅ 選項 A (從第二個檔案引入)
    @Column(columnDefinition = "TEXT")
    private String option1;

    // ✅ 選項 B (從第二個檔案引入)
    @Column(columnDefinition = "TEXT")
    private String option2;

    // ✅ 選項 C (從第二個檔案引入)
    @Column(columnDefinition = "TEXT")
    private String option3;

    // ✅ 選項 D (從第二個檔案引入)
    @Column(columnDefinition = "TEXT")
    private String option4;

    // ✅ 正確答案的文字 (從第二個檔案引入)
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    // ✅ 正確答案的索引（0~3） (從第二個檔案引入)
    @Column(name = "answer_index")
    private Integer answerIndex;

    // ✅ 該題解析說明 (從第二個檔案引入)
    @Column(columnDefinition = "TEXT")
    private String explanation;

    // ✅ 難度（如 easy / medium / hard）(從第二個檔案引入)
    @Column(length = 10)
    private String difficulty;

    // ✅ 在物件持久化（存入資料庫）之前執行此方法
    // 確保 createdAt 和 answeredAt 欄位總是有一個值
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) { // 避免重複設定，如果已經手動設定則不覆蓋
            createdAt = LocalDateTime.now();
        }
        if (answeredAt == null) { // 避免重複設定，如果已經手動設定則不覆蓋
            answeredAt = LocalDateTime.now();
        }
    }

    // ✅ 無參數建構子（JPA 必備）
    public Answer() {}

    // --- 以下為標準 Getter / Setter 區域 ---

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

    public Long getQuizId() {
		return quizId;
	}

	public void setQuizId(Long quizId) {
		this.quizId = quizId;
	}

    public Long getVideoId() {
		return videoId;
	}

	public void setVideoId(Long videoId) {
		this.videoId = videoId;
	}

	public Integer getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Integer selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Boolean getIsCorrect() { // 將 isCorrect() 改為 getIsCorrect() 以符合 JavaBean 規範
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) { // 將 setCorrect() 改為 setIsCorrect()
        isCorrect = correct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
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

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    /**
     * 兼容方法：getAnswer() 實際等同於 getAnswerText()
     * 方便其他程式碼中用 answer 來取代 answerText
     */
    @Transient // 標記為非資料庫欄位，僅為方便存取
    public String getAnswer() {
        return answerText;
    }

    public void setAnswer(String answer) {
        this.answerText = answer;
    }

    public Integer getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(Integer answerIndex) {
        this.answerIndex = answerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    // ✅ 根據 index 取得對應選項文字（0=A、1=B、2=C、3=D）
    @Transient // 標記為非資料庫欄位
    public String getOptionTextByIndex(Integer index) {
        if (index == null) return null;
        switch (index) {
            case 0: return option1;
            case 1: return option2;
            case 2: return option3;
            case 3: return option4;
            default: return null;
        }
    }

    // ✅ 將內部難度英文代碼轉換為中文標籤
    @Transient // 標記為非資料庫欄位
    public String getDifficultyLabel() {
        if (difficulty == null || difficulty.isEmpty()) return "未分類";
        switch (difficulty.toLowerCase()) {
            case "easy": return "簡單";
            case "medium": return "中等";
            case "hard": return "困難";
            default: return difficulty; // 如果有其他值，直接回傳
        }
    }

    // ✅ 根據難度回傳對應顏色（用於前端顯示）
    @Transient // 標記為非資料庫欄位
    public String getDifficultyColor() {
        if (difficulty == null || difficulty.isEmpty()) return "#999";
        switch (difficulty.toLowerCase()) {
            case "easy": return "#4caf50";    // 綠色
            case "medium": return "#ff9800"; // 橙色
            case "hard": return "#f44336";    // 紅色
            default: return "#999";            // 灰色（未知）
        }
    }
}