package com.example.demo.entity; // 你可以根據專案需求選擇合適的套件名稱，例如：com.example.demo.entity

import jakarta.persistence.*;
import java.util.List;

/**
 * ✅ Quiz：選擇題的資料模型，對應資料表 quiz
 *
 * 代表單一題目（與某部影片 video 關聯）
 * 包含題目、4 個選項、正確答案索引、解析、來源、難易度、以及題目類型等資訊
 */
@Entity
@Table(name = "quiz") // 資料表名稱通常用複數，例如 quizzes
public class Quiz {

    // ✅ 主鍵欄位，自動編號（對應 quiz_id）
    // 使用 Long 型態作為主鍵更為通用和推薦
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    // 🔥 修正：如果 videoId 只是影片的ID，不代表它是一個關聯實體，
    // 就移除 @ManyToOne 註解。只保留 @Column。
    // 如果它確實是 ManyToOne，那麼它應該是 Video 實體型別，並使用 @JoinColumn。
    @Column(name = "video_id") // 假設它只是一個普通的影片ID欄位
    private Long videoId;

    // ✅ 題目內容
    @Column(columnDefinition = "TEXT") // 使用 TEXT 類型支援較長的題目內容
    private String question;

    // ✅ 四個選項（必備）
    // 針對選項內容也使用 TEXT 類型以避免長度限制
    @Column(columnDefinition = "TEXT")
    private String option1;

    @Column(columnDefinition = "TEXT")
    private String option2;

    @Column(columnDefinition = "TEXT")
    private String option3;

    @Column(columnDefinition = "TEXT")
    private String option4;

    // ✅ 正確答案的索引值（0~3）
    // 使用 Integer 型態，在某些情況下可以為 null (但對於正確答案通常不建議為 null)
    @Column(name = "correct_index")
    private Integer correctIndex;

    // ✅ 非資料庫欄位，用來傳送正確答案文字（如：「甲是對的」）
    // @Transient 表示此欄位不會被持久化到資料庫
    @Transient
    private String correctAnswer;

    // ✅ 非資料庫欄位，用來傳送正確選項代號（如：「A」）
    // @Transient 表示此欄位不會被持久化到資料庫
    @Transient
    private String correctOption;

    // ✅ 該題的解析說明（例如為何選這一題）
    @Column(columnDefinition = "TEXT") // 使用 TEXT 類型支援較長的解析內容
    private String explanation;

    // ✅ 題目來源（local / GPT）
    @Column(length = 10) // 限制字串長度
    private String source = "local"; // 提供預設值 "local"

    // ✅ 題目難易度（easy / medium / hard）
    @Column(length = 10) // 限制字串長度
    private String difficulty;

    // ✅ 題目類型：單選、多選、填空
    // 使用 @Enumerated(EnumType.STRING) 將 Enum 名稱作為字串儲存到資料庫
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('single','multi','text')") // 定義資料庫中的 ENUM 類型
    private QuizType type = QuizType.single; // 提供預設值 "single"

    // ✅ 定義題目類型列舉
    public enum QuizType {
        single, multi, text
    }

    // ✅ 無參數建構子（JPA 必備）
    public Quiz() {}

    // --- 以下為標準 Getter / Setter 區域 ---

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

    public Integer getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(Integer correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public QuizType getType() {
        return type;
    }

    public void setType(QuizType type) {
        this.type = type;
    }

    // ✅ 提供以 List 傳入選項的 setter（通常用於自動產題時）
    public void setOptions(List<String> options) {
        if (options != null && options.size() >= 4) {
            this.option1 = options.get(0);
            this.option2 = options.get(1);
            this.option3 = options.get(2);
            this.option4 = options.get(3);
        }
    }

    // ✅ 將四個選項包成 List 回傳（用於迴圈顯示等用途）
    public List<String> getOptions() {
        return List.of(option1, option2, option3, option4);
    }
}