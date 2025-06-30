package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Quiz;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;

@Repository // 將此類標記為 Spring 管理的 DAO 元件
public class QuizDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ✅ 寫入單筆 quiz 題目，並回傳資料庫自動產生的 quiz_id
     * 用於儲存單一 GPT 題目或本地題目到 quiz 表
     */
    public int insertQuiz(Quiz quiz) {
        String sql = "INSERT INTO quiz (video_id, question, option1, option2, option3, option4, correct_index, explanation, source, difficulty) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // 建立 keyHolder 以接收自動產生的 quiz_id（PK）
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // 使用 PreparedStatement 手動綁定每個欄位值
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, quiz.getVideoId());
            ps.setString(2, quiz.getQuestion());
            ps.setString(3, quiz.getOption1());
            ps.setString(4, quiz.getOption2());
            ps.setString(5, quiz.getOption3());
            ps.setString(6, quiz.getOption4());
            ps.setInt(7, quiz.getCorrectIndex());
            ps.setString(8, quiz.getExplanation());
            ps.setString(9, quiz.getSource());
            ps.setString(10, quiz.getDifficulty());
            return ps;
        }, keyHolder);

        // 取得自動產生的主鍵值（quiz_id）
        return keyHolder.getKey().intValue();
    }

    /**
     * ✅ 根據影片 ID、題目來源查詢題庫，最多回傳 limit 筆（順序由 quiz_id 決定）
     */
    public List<Quiz> getQuizzesByVideoIdAndSource(String videoId, String source, int limit) {
        String sql = "SELECT * FROM quiz WHERE video_id = ? AND source = ? ORDER BY quiz_id LIMIT ?";
        return jdbcTemplate.query(sql, new Object[]{videoId, source, limit}, this::mapRowToQuiz);
    }

    /**
     * ✅ 額外補充：依 quiz_id 倒序，查詢最新的題目清單（適合查看 GPT 最近產生的）
     */
    public List<Quiz> getLatestQuizzesByVideoAndSource(String videoId, String source, int limit) {
        String sql = "SELECT * FROM quiz WHERE video_id = ? AND source = ? ORDER BY quiz_id DESC LIMIT ?";
        return jdbcTemplate.query(sql, new Object[]{videoId, source, limit}, this::mapRowToQuiz);
    }

    /**
     * ✅ 將一筆資料從 ResultSet 映射成 Quiz Java 物件
     * 每次查詢 quiz 表都會用到這個 mapper
     */
    private Quiz mapRowToQuiz(ResultSet rs, int rowNum) {
        try {
            Quiz quiz = new Quiz();
            quiz.setQuizId(rs.getLong("quiz_id"));
            quiz.setVideoId(rs.getLong("video_id"));
            quiz.setQuestion(rs.getString("question"));
            quiz.setOption1(rs.getString("option1"));
            quiz.setOption2(rs.getString("option2"));
            quiz.setOption3(rs.getString("option3"));
            quiz.setOption4(rs.getString("option4"));
            quiz.setCorrectIndex(rs.getInt("correct_index"));
            quiz.setExplanation(rs.getString("explanation"));
            quiz.setSource(rs.getString("source"));
            quiz.setDifficulty(rs.getString("difficulty"));
            return quiz;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ 寫入 quiz 清單，並避免重複（以 videoId + question 判斷是否已存在）
     * 適合批次匯入自動產生題目時使用
     * @return 實際寫入成功的題目數量
     */
    public int insertQuizListAvoidDuplicate(String videoId, List<Quiz> quizList, String source) {
        int insertCount = 0;

        // 判斷是否已存在該題（以 question + videoId 為條件）
        String checkSql = "SELECT COUNT(*) FROM quiz WHERE video_id = ? AND question = ?";

        // 若不存在，再進行寫入
        String insertSql = """
            INSERT INTO quiz (video_id, question, option1, option2, option3, option4,
                              correct_index, explanation, source, difficulty)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        for (Quiz quiz : quizList) {
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, videoId, quiz.getQuestion());
            if (count != null && count == 0) {
                jdbcTemplate.update(insertSql,
                    quiz.getVideoId(), quiz.getQuestion(),
                    quiz.getOption1(), quiz.getOption2(), quiz.getOption3(), quiz.getOption4(),
                    quiz.getCorrectIndex(), quiz.getExplanation(), source, quiz.getDifficulty()
                );
                insertCount++;
            }
        }

        return insertCount;
    }

    /**
     * ✅ 查詢題庫：依據影片、來源與難度，限制回傳題目筆數
     * 用於支援「難度篩選」功能的載題
     */
    public List<Quiz> getQuizzesByVideoSourceDifficulty(String videoId, String source, String difficulty, int limit) {
        String sql = """
            SELECT * FROM quiz
            WHERE video_id = ? AND source = ? AND difficulty = ?
            ORDER BY quiz_id
            LIMIT ?
        """;

        return jdbcTemplate.query(sql, new Object[]{videoId, source, difficulty, limit}, this::mapRowToQuiz);
    }

}
