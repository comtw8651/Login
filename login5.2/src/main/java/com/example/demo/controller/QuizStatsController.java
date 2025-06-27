package com.example.demo.controller;

// 匯入 JSON 回傳工具（Gson）
import com.google.gson.JsonObject;

// 匯入 Spring 相關元件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 類別
import com.example.demo.dao.QuizResultDAO;

/**
 * QuizStatsController：
 * 回傳指定使用者在特定影片中的測驗統計資料（答題總數與正確數）
 *
 * 路徑範例：
 * GET /api/quizStats?videoId=xxx&userId=1&source=gpt
 *
 * 回傳範例：
 * {
 *   "total": 10,
 *   "correct": 8,
 *   "accuracy": "80.00"
 * }
 */
@RestController // 設定為 REST 控制器，回傳 JSON 字串
@RequestMapping("/api") // 所有 API 路徑以 /api 為前綴
public class QuizStatsController {

    @Autowired // 注入測驗結果 DAO
    private QuizResultDAO quizResultDAO;

    /**
     * 根據使用者 ID、影片 ID 與題目來源，統計總答題數與答對數
     * @param videoId 影片 ID（如 YouTube ID）
     * @param userId 使用者 ID
     * @param source 題目來源（gpt、local，可省略）
     * @return JSON 字串：total、correct、accuracy%
     */
    @GetMapping("/quizStats")
    public String getQuizStats(
            @RequestParam String videoId,
            @RequestParam int userId,
            @RequestParam(required = false) String source) {

        try {
            // 查詢答題總數
            int total = quizResultDAO.sumTotalQuestions(userId, videoId, source);

            // 查詢答對題數
            int correct = quizResultDAO.sumCorrectAnswers(userId, videoId, source);

            // 建立回傳 JSON 結果
            JsonObject json = new JsonObject();
            json.addProperty("total", total);    // 總答題數
            json.addProperty("correct", correct); // 答對題數

            // 計算正確率（若無題目則顯示 N/A）
            json.addProperty("accuracy", total > 0
                    ? String.format("%.2f", (double) correct / total * 100)
                    : "N/A");

            return json.toString(); // 回傳 JSON 結果

        } catch (Exception e) {
            // 發生錯誤時回傳錯誤 JSON
            JsonObject error = new JsonObject();
            error.addProperty("error", "統計錯誤：" + e.getMessage());
            return error.toString();
        }
    }
}
