package com.example.demo.controller;

// 匯入 JSON 組裝工具（Gson）
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// 匯入 Spring Boot 組件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與模型
import com.example.demo.dao.QuizResultDAO;
import com.example.demo.entity.QuizResultSummary;

import java.util.List;

/**
 * QuizHistoryController：查詢使用者歷史測驗紀錄的摘要清單
 * 功能說明：
 * 提供前端使用者的測驗紀錄統計資訊（依影片與來源分組）
 * 路徑：GET /api/quizHistory?userId=xxx
 * 回傳格式範例（JSON 陣列）：
 * [
 *   {
 *     "videoId": "abc123",
 *     "videoTitle": "Java 基礎教學",
 *     "source": "gpt",
 *     "totalQuizCount": 30,
 *     "total": 10,
 *     "correct": 8
 *   },
 *   ...
 * ]
 */
@RestController // 註明為 REST 控制器，回傳 JSON 格式資料
@RequestMapping("/api") // 所有 API 路徑都以 /api 為前綴
public class QuizHistoryController {

    @Autowired // 自動注入 quizResultDAO，操作 quiz_results 資料表
    private QuizResultDAO quizResultDAO;

    /**
     * 查詢指定使用者的測驗紀錄摘要
     * @param userId 使用者 ID
     * @return 回傳 JSON 陣列字串，包含每部影片與來源的答題統計
     */
    @GetMapping("/quizHistory")
    public String getQuizHistory(@RequestParam("userId") int userId) {
        JsonArray jsonArr = new JsonArray(); // 建立回傳用的 JSON 陣列

        try {
            // 查詢該使用者的所有測驗紀錄彙總（依影片與來源分組）
            List<QuizResultSummary> resultList = quizResultDAO.getQuizSummaryByUser(userId);

            // 逐筆處理每部影片的測驗摘要
            for (QuizResultSummary result : resultList) {
                JsonObject obj = new JsonObject();

                obj.addProperty("videoId", result.getVideoId());                // 影片 ID（非主鍵，是 YouTube ID）
                obj.addProperty("videoTitle", result.getVideoTitle());          // 影片標題
                obj.addProperty("source", result.getSource());                  // 題目來源（local/gpt）
                obj.addProperty("totalQuizCount", result.getTotalQuizCount());  // 題庫中該影片的總題數
                obj.addProperty("total", result.getTotal());                    // 使用者實際作答總題數
                obj.addProperty("correct", result.getCorrect());                // 使用者答對題數

                jsonArr.add(obj); // 加入結果陣列
            }

        } catch (Exception e) {
            // 若發生例外，回傳錯誤 JSON 物件
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", "伺服器錯誤：" + e.getMessage());
            return error.toString();
        }

        // 將完整 JSON 陣列轉為字串並回傳
        return jsonArr.toString();
    }
}
