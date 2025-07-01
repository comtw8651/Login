package com.example.demo.controller;

// 匯入 Gson 套件，用來組合 JSON 回傳資料
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// 匯入 Spring Boot Web 與 DI 元件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與資料模型
import com.example.demo.dao.QuizResultDAO;
import com.example.demo.entity.QuizResultSummary;

import java.util.List;

/**
 * QuizSummaryController：
 * 回傳使用者在所有影片的測驗總結資訊
 * 路徑：GET /api/quizSummary?userId=xxx
 * 回傳格式：
 * [
 *   {
 *     "title": "影片名稱",
 *     "videoId": "abc123",
 *     "source": "gpt",
 *     "totalQuizCount": 30,
 *     "totalQuestions": 10,
 *     "correctAnswers": 7
 *   },
 *   ...
 * ]
 */
@RestController // 標示為 REST API 控制器，直接回傳 JSON
@RequestMapping("/api") // 所有 API 路徑前綴為 /api
public class QuizSummaryController {

    @Autowired // 自動注入 QuizResultDAO，用來查詢測驗統計資料
    private QuizResultDAO quizResultDAO;

    /**
     * 根據使用者 ID 查詢每部影片的作答統計摘要（依影片與來源分組）
     * @param userId 使用者 ID（前端傳入）
     * @return JSON 陣列字串（每筆為一部影片的答題統計）
     */
    @GetMapping("/quizSummary")
    public String getQuizSummary(@RequestParam int userId) {
        try {
            // 取得該使用者的測驗統計資料清單（分影片與來源）
            List<QuizResultSummary> list = quizResultDAO.getQuizSummaryByUser(userId);

            JsonArray arr = new JsonArray(); // 建立 JSON 陣列用來回傳

            for (QuizResultSummary q : list) {
                JsonObject obj = new JsonObject();

                obj.addProperty("title", q.getVideoTitle());               // 影片標題
                obj.addProperty("videoId", q.getVideoId());                // 影片 ID（如 YouTube ID）
                obj.addProperty("source", q.getSource());                  // 題目來源（gpt/local）
                obj.addProperty("totalQuizCount", q.getTotalQuizCount());  // 題庫中該影片的總題數
                obj.addProperty("totalQuestions", q.getTotal());           // 使用者實際作答總題數
                obj.addProperty("correctAnswers", q.getCorrect());         // 使用者答對的題數

                arr.add(obj); // 加入陣列
            }

            return arr.toString(); // 回傳 JSON 陣列字串給前端

        } catch (Exception e) {
            // 若查詢過程出現例外，回傳錯誤訊息 JSON
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", "統計資料查詢失敗：" + e.getMessage());
            return error.toString();
        }
    }
}
