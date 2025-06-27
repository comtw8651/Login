package com.example.demo.controller;

// 匯入 Gson 用於 JSON 建構
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// 匯入 Spring Boot 框架相關註解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與模型
import com.example.demo.dao.QuizDAO;
import com.example.demo.entity.Quiz;

import java.util.List;

/**
 * LoadQuizController：題目載入控制器
 * 根據指定影片 ID（videoId）、題目來源（source）與難度（difficulty）載入測驗題目（最多 5 題）
 *
 * 路徑範例：
 * GET /api/loadQuiz?videoId=xxx&source=gpt&difficulty=easy
 *
 * 回傳範例（JSON 陣列）：
 * [
 *   {
 *     "id": 123,
 *     "videoId": "abc123",
 *     "question": "Java 中如何宣告變數？",
 *     "options": ["int a;", "int = a;", ...],
 *     "answer": "int a;",
 *     "source": "gpt"
 *   },
 *   ...
 * ]
 */
@RestController // 標示為 REST 控制器，回傳純 JSON
@RequestMapping("/api") // 所有 API 路徑以 /api 為前綴
public class LoadQuizController {

    @Autowired // 自動注入 quizDAO 實例
    private QuizDAO quizDAO;

    /**
     * 載入指定影片與來源的題庫資料，最多限制 5 題
     * @param videoId 影片 ID（非數字主鍵，是 YouTube ID 或資料庫欄位 videoId）
     * @param source 題目來源（gpt 或 local）
     * @param difficulty 題目難度（easy / medium / hard）
     * @return 題目陣列 JSON 字串
     */
    @GetMapping("/loadQuiz")
    public String loadQuiz(@RequestParam("videoId") String videoId,
                           @RequestParam("source") String source,
                           @RequestParam("difficulty") String difficulty) {

        JsonArray quizArray = new JsonArray(); // 最終回傳用的 JSON 陣列

        // ❗️需確認 quizDAO 有此方法：支援多條件查詢（影片 ID、來源、難度、限制筆數）
        List<Quiz> quizList = quizDAO.getQuizzesByVideoSourceDifficulty(videoId, source, difficulty, 5);

        // 將每一題組成 JSON 格式加入 quizArray
        for (Quiz quiz : quizList) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", quiz.getId());                  // 題目 ID
            obj.addProperty("videoId", videoId);                  // 對應影片 ID
            obj.addProperty("question", quiz.getQuestion());      // 題目內容

            JsonArray options = new JsonArray();                  // 選項陣列
            options.add(quiz.getOption1());
            options.add(quiz.getOption2());
            options.add(quiz.getOption3());
            options.add(quiz.getOption4());
            obj.add("options", options);                          // 加入選項

            obj.addProperty("answer", quiz.getCorrectOption());   // 正確答案（文字）
            obj.addProperty("source", quiz.getSource());          // 題目來源（gpt 或 local）

            quizArray.add(obj); // 加入主陣列
        }

        return quizArray.toString(); // 轉為 JSON 字串回傳給前端
    }
}
