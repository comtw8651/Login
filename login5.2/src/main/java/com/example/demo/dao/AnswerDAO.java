package com.example.demo.dao;

// 匯入 Spring JDBC 與元件管理
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// 匯入模型類別
import com.example.demo.entity.Answer;
import com.example.demo.entity.AnswerRecord;
import com.example.demo.entity.AttemptGroup;

import java.util.*;

@Repository // DAO 元件註冊，讓 Spring 管理
public class AnswerDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ➕ 新增一筆作答記錄（answer 表）
     * 寫入詳細內容包含選項、答案、解析等
     */
    public void insertAnswer(Answer answer) {
        System.out.println("➕ 寫入答案：" + answer.getQuestion() + " | 難度：" + answer.getDifficulty());
        String sql = """
            INSERT INTO answer (
                user_id, quiz_id, selected_option, is_correct, source,
                created_at, answered_at, question, option1, option2, option3, option4,
                video_id, answer_text, answer_index, explanation, attempt_id, difficulty
            ) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Object[] params = new Object[] {
            answer.getUserId(), answer.getQuizId(), answer.getSelectedOption(),
            answer.getIsCorrect(), answer.getSource(),
            answer.getQuestion(), answer.getOption1(), answer.getOption2(),
            answer.getOption3(), answer.getOption4(), answer.getVideoId(),
            answer.getAnswerText(), answer.getAnswerIndex(), answer.getExplanation(),
            answer.getAttemptId(), answer.getDifficulty()
        };

        jdbcTemplate.update(sql, params);
    }

    /**
     * ✅ 根據 quiz_id 與來源查詢正確選項的 index
     */
    public Integer getCorrectAnswerIndex(int quizId, String source) {
        String sql = "SELECT correct_index FROM quiz WHERE quiz_id = ? AND source = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, quizId, source);
    }

    /**
     * 📦 根據 quiz_id 查詢 quiz 的完整資料，用於填入 answer 詳解欄位
     */
    public Answer getAnswerDetails(int quizId) {
        String sql = """
            SELECT
                quiz_id AS quizId,
                question, option1, option2, option3, option4,
                correct_index AS answerIndex,
                explanation, source, difficulty
            FROM quiz
            WHERE quiz_id = ?
        """;

        List<Answer> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Answer.class), quizId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 📄 查詢某使用者對某影片的所有作答紀錄（含 quiz 整合）
     */
    public List<AnswerRecord> getAnswersByUser(int userId, String videoId) {
        String sql = """
            SELECT a.quiz_id, a.selected_option, a.source,
                   COALESCE(a.answer_index, q.correct_index, -1) AS answer_index,
                   COALESCE(a.question, q.question) AS question,
                   COALESCE(a.option1, q.option1) AS option1,
                   COALESCE(a.option2, q.option2) AS option2,
                   COALESCE(a.option3, q.option3) AS option3,
                   COALESCE(a.option4, q.option4) AS option4,
                   COALESCE(a.explanation, q.explanation) AS explanation,
                   a.answer_text AS answerText
            FROM answer a
            LEFT JOIN quiz q ON a.quiz_id = q.quiz_id
            WHERE a.user_id = ? AND a.video_id = ?
            ORDER BY a.id ASC
        """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AnswerRecord.class), userId, videoId);
    }

    /**
     * ✅ 統計某次測驗中使用者答對題數
     */
    public int countCorrectAnswers(int userId, String videoId, long attemptId) {
        String sql = """
            SELECT COUNT(*) FROM answer
            WHERE user_id = ? AND video_id = ? AND attempt_id = ? AND is_correct = true
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, videoId, attemptId);
    }

    /**
     * ❌ 刪除某使用者在某影片下的所有作答紀錄
     */
    public void deleteAnswersByUser(int userId, String videoId) {
        String sql = "DELETE FROM answer WHERE user_id = ? AND video_id = ?";
        jdbcTemplate.update(sql, userId, videoId);
    }

    /**
     * ❌ 刪除某次測驗（attempt）所有作答紀錄
     */
    public void deleteAnswersByAttempt(int userId, String videoId, long attemptId) {
        String sql = "DELETE FROM answer WHERE user_id = ? AND video_id = ? AND attempt_id = ?";
        jdbcTemplate.update(sql, userId, videoId, attemptId);
    }

    /**
     * 🔍 根據影片 ID 查詢影片標題（使用 video 表）
     */
    public String getVideoTitle(String videoId) {
        String sql = "SELECT title FROM video WHERE video_id = ?";
        List<String> titles = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("title"), videoId);
        return titles.isEmpty() ? "未知影片" : titles.get(0);
    }

    /**
     * 📝 查詢指定使用者、影片、attempt 的所有作答紀錄（含 quiz 資料）
     */
    public List<Answer> getAnswersByUserAndAttemptId(int userId, String videoId, long attemptId) {
        String sql = """
            SELECT
                a.quiz_id, a.selected_option, a.is_correct, a.source,
                a.user_id, a.video_id, a.attempt_id,
                COALESCE(a.question, q.question) AS question,
                COALESCE(a.option1, q.option1) AS option1,
                COALESCE(a.option2, q.option2) AS option2,
                COALESCE(a.option3, q.option3) AS option3,
                COALESCE(a.option4, q.option4) AS option4,
                COALESCE(a.answer_index, q.correct_index) AS answer_index,
                COALESCE(a.explanation, q.explanation) AS explanation,
                q.difficulty,
                CASE a.selected_option
                    WHEN 0 THEN COALESCE(q.option1, a.option1)
                    WHEN 1 THEN COALESCE(q.option2, a.option2)
                    WHEN 2 THEN COALESCE(q.option3, a.option3)
                    WHEN 3 THEN COALESCE(q.option4, a.option4)
                    ELSE NULL
                END AS answer_text
            FROM answer a
            LEFT JOIN quiz q ON a.quiz_id = q.quiz_id
            WHERE a.user_id = ? AND a.video_id = ? AND a.attempt_id = ?
            ORDER BY a.id ASC
        """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Answer.class), userId, videoId, attemptId);
    }

    /**
     * 📊 查詢使用者所有影片的作答記錄（以影片 ID 與測驗次數 attempt 分組）
     */
    public Map<String, List<AttemptGroup>> getAnswersGroupedByUserAcrossVideos(int userId) {
        String sql = """
            SELECT
                video_id, attempt_id, answered_at,
                quiz_id, selected_option, answer_index,
                question, option1, option2, option3, option4,
                source, answer_text AS answer, explanation
            FROM answer
            WHERE user_id = ?
            ORDER BY video_id, attempt_id DESC, answered_at
        """;

        List<AnswerRecord> all = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AnswerRecord.class), userId);

        // 以影片 ID 為 key、每部影片下放入多個 attempt 群組
        Map<String, List<AttemptGroup>> grouped = new LinkedHashMap<>();

        for (AnswerRecord record : all) {
            String videoId = record.getVideoId();
            long attemptId = record.getAttemptId();

            grouped.putIfAbsent(videoId, new ArrayList<>());
            List<AttemptGroup> groupList = grouped.get(videoId);

            // 查詢是否已有該 attemptId 的群組
            AttemptGroup attemptGroup = groupList.stream()
                    .filter(g -> g.getAttemptId() == attemptId)
                    .findFirst()
                    .orElse(null);

            if (attemptGroup == null) {
                attemptGroup = new AttemptGroup();
                attemptGroup.setAttemptId(attemptId);
                attemptGroup.setSubmittedAt(null); // ⏱ 如需顯示時間可額外補查
                attemptGroup.setQuestions(new ArrayList<>());
                groupList.add(attemptGroup);
            }

            attemptGroup.getQuestions().add(record); // 加入該題
        }

        return grouped;
    }

    /**
     * ✅ 寫入 quiz_results 表（一筆測驗總結資料）
     */
    public void insertQuizResult(int userId, String videoId, long attemptId, int totalQuestions, int correctAnswers,
                                 String accuracy, String source) {
        String sql = """
            INSERT INTO quiz_results (
                user_id, video_id,
                total_questions, correct_answers,
                attempt_id,
                accuracy, source,
                submitted_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
        """;

        jdbcTemplate.update(sql, userId, videoId, totalQuestions, correctAnswers, attemptId, accuracy, source);
    }

    /**
     * ✨ 增加使用者的經驗值（exp 欄位加上指定數值）
     */
    public void addExp(int userId, int expToAdd) {
        String sql = "UPDATE users SET exp = exp + ? WHERE id = ?";
        jdbcTemplate.update(sql, expToAdd, userId);
    }
}
