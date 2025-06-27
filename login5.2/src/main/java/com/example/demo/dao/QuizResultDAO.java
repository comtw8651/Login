package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.QuizResult;
import com.example.demo.entity.QuizResultSummary;

import java.util.List;

@Repository // 告訴 Spring 這是資料庫存取元件（DAO）
public class QuizResultDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ✅ 儲存一次完整的測驗結果（寫入 quiz_results 表）
     * 含答對題數、總題數、來源、影片 ID、使用者 ID、難度等
     */
    public void insertQuizResult(int userId, String videoId, int correctCount, int totalCount, String source, long attemptId, String difficulty) {
        String sql = """
            INSERT INTO quiz_results 
            (user_id, video_id, correct_answers, total_questions, submitted_at, source, attempt_id, difficulty) 
            VALUES (?, ?, ?, ?, NOW(), ?, ?, ?)
        """;
        jdbcTemplate.update(sql, userId, videoId, correctCount, totalCount, source, attemptId, difficulty);
    }

    /**
     * ✅ 查詢某位使用者針對某部影片「最近一次」的測驗結果（依時間排序）
     */
    public QuizResult getLatestQuizResult(int userId, String videoId) {
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? AND video_id = ? ORDER BY submitted_at DESC LIMIT 1";
        List<QuizResult> list = jdbcTemplate.query(sql, new Object[]{userId, videoId}, quizResultMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * ✅ 查詢使用者所有測驗結果（最新的排在最前面）
     */
    public List<QuizResult> getAllResultsByUser(int userId) {
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? ORDER BY submitted_at DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, quizResultMapper);
    }

    /**
     * ✅ 同上，只是方法名稱不同（做法一樣）
     */
    public List<QuizResult> getResultsByUser(int userId) {
        return getAllResultsByUser(userId);
    }

    /**
     * ✅ 依影片統計測驗紀錄（總次數、總題數、正確數）
     * 若同一影片含多種來源（gpt, local），則會標示為「混合」
     * 用於 quizSummary 頁面顯示每部影片的整體表現
     */
    public List<QuizResultSummary> getQuizSummaryByUser(int userId) {
        String sql = """
            SELECT 
                q.video_id,
                v.title AS video_title,
                CASE 
                  WHEN COUNT(DISTINCT q.source) = 1 THEN MAX(q.source)
                  ELSE '混合'
                END AS source,
                COUNT(*) AS total_quiz_count,
                SUM(q.total_questions) AS total,
                SUM(q.correct_answers) AS correct
            FROM quiz_results q
            JOIN video v ON q.video_id = v.video_id
            WHERE q.user_id = ?
            GROUP BY q.video_id, v.title
        """;

        return jdbcTemplate.query(sql, new Object[]{userId}, quizResultSummaryMapper);
    }

    // ----------------- RowMapper 區 -----------------

    /**
     * ✅ 將 quiz_results 表轉換成 QuizResult 物件
     * 用於顯示每次測驗的詳細資料
     */
    private final RowMapper<QuizResult> quizResultMapper = (rs, rowNum) -> {
        QuizResult result = new QuizResult();
        result.setUserId(rs.getLong("user_id"));
        result.setVideoId(rs.getLong("video_id"));
        result.setCorrectAnswers(rs.getInt("correct_answers"));
        result.setTotalQuestions(rs.getInt("total_questions"));
        result.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
        result.setSource(rs.getString("source"));
        result.setAttemptId(rs.getLong("attempt_id"));
        return result;
    };

    /**
     * ✅ 將 quiz 統計結果轉成 QuizResultSummary 物件
     * 用於影片總結分析區塊的資料呈現
     */
    private final RowMapper<QuizResultSummary> quizResultSummaryMapper = (rs, rowNum) -> {
        QuizResultSummary summary = new QuizResultSummary();
        summary.setVideoId(rs.getString("video_id"));
        summary.setVideoTitle(rs.getString("video_title"));
        summary.setSource(rs.getString("source"));
        summary.setTotalQuizCount(rs.getInt("total_quiz_count"));
        summary.setTotal(rs.getInt("total"));
        summary.setCorrect(rs.getInt("correct"));
        return summary;
    };

    /**
     * ✅ 統計使用者對某影片某來源的總作答數（answer 表）
     * 用於 accuracy 百分比計算
     */
    public int sumTotalQuestions(int userId, String videoId, String source) {
        String sql = """
            SELECT COUNT(*) FROM answer 
            WHERE user_id = ? AND video_id = ? AND source = ?
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, videoId, source);
    }

    /**
     * ✅ 統計使用者對某影片某來源的正確作答數（answer 表）
     */
    public int sumCorrectAnswers(int userId, String videoId, String source) {
        String sql = """
            SELECT COUNT(*) FROM answer 
            WHERE user_id = ? AND video_id = ? AND source = ? AND is_correct = true
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, videoId, source);
    }
}
