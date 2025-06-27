package com.example.demo.controller;

// 匯入 JSON 處理相關類別（Gson）
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// 匯入 Servlet 與 Spring 相關功能
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與資料模型
import com.example.demo.dao.AnswerDAO;
import com.example.demo.entity.Answer;

import java.io.BufferedReader;
import java.util.*;

/**
 * SubmitAnswerController：
 * 接收前端作答資料（答案陣列），儲存至資料庫，
 * 並自動計算正確率、更新 quiz_results 與經驗值
 *
 * 路徑：POST /api/submitAnswer
 * 請求格式：JSON 含 userId, videoId, attemptId, difficulty, answers[]
 * 回傳格式：Map → 自動轉為 JSON，包含統計結果與成功/錯誤回報
 */
@RestController
@RequestMapping("/api")
public class SubmitAnswerController {

    @Autowired
    private AnswerDAO answerDAO;

    /**
     * 接收作答資料，逐題寫入資料庫，統計正確率與經驗值，並回傳結果
     */
    @PostMapping("/submitAnswer")
    public ResponseEntity<Map<String, Object>> submitAnswer(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>(); // 回傳用的 JSON Map
        int insertCount = 0, failCount = 0, correctCount = 0;
        int gainedExp = 0;  // 🔥 累積獲得的經驗值

        try (BufferedReader reader = request.getReader()) {
            // 讀取原始 JSON 字串
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }

            System.out.println("[submitAnswer] 收到 JSON: " + jsonBuffer);

            // 解析 JSON 成物件
            JsonObject jsonObject = JsonParser.parseString(jsonBuffer.toString()).getAsJsonObject();

            int userId = jsonObject.get("userId").getAsInt();
            String videoId = jsonObject.get("videoId").getAsString();
            long attemptId = jsonObject.get("attemptId").getAsLong();
            String difficulty = jsonObject.has("difficulty")
                    ? jsonObject.get("difficulty").getAsString()
                    : "unknown";

            // 檢查 answers 陣列是否存在
            if (!jsonObject.has("answers") || !jsonObject.get("answers").isJsonArray()) {
                response.put("success", false);
                response.put("message", "❌ 缺少 answers 陣列");
                return ResponseEntity.badRequest().body(response);
            }

            JsonArray answersArray = jsonObject.getAsJsonArray("answers");

            // 逐題處理作答資料
            for (int i = 0; i < answersArray.size(); i++) {
                JsonObject answerJson = answersArray.get(i).getAsJsonObject();

                // 檢查是否缺欄位
                if (!answerJson.has("quizId") || !answerJson.has("selectedIndex")) {
                    System.out.println("⚠️ 缺少 quizId 或 selectedIndex，跳過");
                    continue;
                }

                int quizId = answerJson.get("quizId").getAsInt();
                int selectedIndex = answerJson.get("selectedIndex").getAsInt();

                // 取得完整題目資訊
                Answer fullAnswer = answerDAO.getAnswerDetails(quizId);
                if (fullAnswer == null) {
                    System.out.println("⚠️ 找不到題目 ID: " + quizId + "，跳過");
                    continue;
                }

                // 取得正確答案索引，並比對是否正確
                int correctIndex = answerDAO.getCorrectAnswerIndex(quizId, fullAnswer.getSource());
                boolean isCorrect = (selectedIndex == correctIndex);

                // 建立 Answer 實例並填入所有欄位
                Answer answer = new Answer();
                answer.setUserId(userId);
                answer.setVideoId(videoId);
                answer.setAttemptId(attemptId);
                answer.setQuizId(quizId);
                answer.setSelectedOption(selectedIndex);
                answer.setCorrect(isCorrect);
                answer.setSource(fullAnswer.getSource());
                answer.setQuestion(fullAnswer.getQuestion());
                answer.setOption1(fullAnswer.getOption1());
                answer.setOption2(fullAnswer.getOption2());
                answer.setOption3(fullAnswer.getOption3());
                answer.setOption4(fullAnswer.getOption4());
                answer.setAnswer(fullAnswer.getAnswer());
                answer.setAnswerIndex(correctIndex);
                answer.setExplanation(fullAnswer.getExplanation());
                answer.setDifficulty(difficulty);

                // 嘗試插入答案資料到資料庫
                try {
                    answerDAO.insertAnswer(answer);
                    insertCount++; // 成功筆數 +1

                    if (isCorrect) {
                        correctCount++;      // 答對 +1
                        gainedExp += 10;     // ✅ 每答對一題加 10 EXP
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    failCount++; // 插入失敗 +1
                }
            }

            // ✨ 計算正確率
            int total = insertCount;
            double accuracy = total > 0 ? (correctCount * 100.0) / total : 0;
            String accuracyStr = String.format("%.1f", accuracy); // 小數一位

            // 嘗試取得題目來源（用第一題取得）
            String source = "unknown";
            if (answersArray.size() > 0 && answersArray.get(0).getAsJsonObject().has("quizId")) {
                int firstQuizId = answersArray.get(0).getAsJsonObject().get("quizId").getAsInt();
                Answer firstAnswer = answerDAO.getAnswerDetails(firstQuizId);
                if (firstAnswer != null && firstAnswer.getSource() != null) {
                    source = firstAnswer.getSource();
                }
            }

            // ✅ 寫入 quiz_results（一次測驗的總紀錄）
            try {
                answerDAO.insertQuizResult(userId, videoId, attemptId, total, correctCount, accuracyStr, source);
                System.out.println("✅ 成功寫入 quiz_results");
            } catch (Exception e) {
                System.out.println("⚠️ quiz_results 寫入失敗：" + e.getMessage());
            }

            // ✅ 累加經驗值（全部答對完再加）
            if (gainedExp > 0) {
                try {
                    answerDAO.addExp(userId, gainedExp);
                    System.out.println("✨ 獲得 EXP: " + gainedExp);
                } catch (Exception e) {
                    System.out.println("⚠️ 加 EXP 失敗：" + e.getMessage());
                }
            }

            // ✅ 組成回傳統計結果
            response.put("success", true);
            response.put("inserted", insertCount);      // 成功寫入幾筆
            response.put("skipped", failCount);         // 略過幾筆
            response.put("correctCount", correctCount); // 答對幾題
            response.put("total", total);               // 題目總數
            response.put("gainedExp", gainedExp);       // 獲得 EXP
            response.put("accuracy", accuracyStr);      // 正確率
            response.put("message", "✅ 儲存 " + insertCount + " 筆答案，略過 " + failCount + " 筆");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "❌ 發生例外錯誤：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
