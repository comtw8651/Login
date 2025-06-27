package com.example.demo.controller;

// 匯入 Spring 相關套件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與模型類別
import com.example.demo.dao.QuizDAO;
import com.example.demo.entity.Quiz;

import java.util.List;

/**
 * ImportQuizController：匯入題目 API
 * 功能：將題目清單（Quiz List）匯入資料庫，避免重複題目
 * 路徑：POST /api/importQuiz
 * 必填參數：videoId（影片 ID）、quizList（題目 JSON 陣列）
 * 可選參數：source（預設為 "local"）
 */
@RestController // 註明為 REST API 控制器，回傳 JSON 或純文字
@RequestMapping("/api") // 所有 API 路徑以 /api 為前綴
public class ImportQuizController {

    @Autowired // 自動注入 QuizDAO 實例
    private QuizDAO quizDAO;

    /**
     * 匯入題目清單，避免重複題目（依據 question 判斷）
     * @param videoId 影片 ID（必填）
     * @param quizList 題目清單（由前端以 JSON 陣列傳入）
     * @param source 題目來源（可為 local、gpt 等，預設為 local）
     * @return 匯入成功或失敗訊息
     */
    @PostMapping("/importQuiz")
    public ResponseEntity<?> importQuiz(@RequestParam String videoId,
                                        @RequestBody List<Quiz> quizList,
                                        @RequestParam(required = false, defaultValue = "local") String source) {

        // 檢查 videoId 是否提供
        if (videoId == null || videoId.isEmpty()) {
            return ResponseEntity.badRequest().body("缺少 videoId");
        }

        // 檢查題目清單是否為空
        if (quizList == null || quizList.isEmpty()) {
            return ResponseEntity.badRequest().body("題目清單為空");
        }

        try {
            // 呼叫 DAO 實作方法：避免重複地批次新增題目
            int insertedCount = quizDAO.insertQuizListAvoidDuplicate(videoId, quizList, source);

            // 回傳成功訊息
            return ResponseEntity.ok("成功新增 " + insertedCount + " 筆題目");

        } catch (Exception e) {
            // 發生錯誤時的處理與訊息
            e.printStackTrace();
            return ResponseEntity.status(500).body("匯入題庫時發生錯誤");
        }
    }
}
