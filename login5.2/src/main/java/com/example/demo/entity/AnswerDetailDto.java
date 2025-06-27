package com.example.demo.entity;

/**
 * ✅ AnswerDetailDto：用於封裝「使用者單題作答詳解」的資料傳輸物件（DTO）
 * 
 * 此類別不是對應資料庫的實體，而是專門給 API 回傳用：
 * - 將選項、答案都轉為「文字型態」
 * - 提供前端展示「答對/錯 + 解釋內容」使用
 * 
 * 包含：
 * - 題目內容
 * - 四個選項
 * - 正確答案（文字與 index）
 * - 使用者選擇（文字與 index）
 * - 題目來源（GPT / local）
 * - 題目解析
 */
public class AnswerDetailDto {

    // --- 題目與選項文字 ---
    private String question;         // 題目內容
    private String option1;          // 選項 A
    private String option2;          // 選項 B
    private String option3;          // 選項 C
    private String option4;          // 選項 D

    // --- 答案資訊 ---
    private String correct;          // 正確答案（文字，例如「選項 B」）
    private String selected;         // 使用者選擇的文字（例如「選項 A」）
    private Integer correctIndex;    // 正確答案的索引（0~3）
    private Integer selectedIndex;   // 使用者選擇的索引（0~3）

    // --- 額外資訊 ---
    private String source;           // 題目來源（GPT / local）
    private String explanation;      // 題目解析文字

    // =============================
    // 以下為標準 Getter / Setter 區
    // =============================

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

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public Integer getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(Integer correctIndex) {
        this.correctIndex = correctIndex;
    }

    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
