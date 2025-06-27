package com.example.demo.controller;

// 匯入 JSON 組裝工具（使用 Gson）
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// 匯入 Spring 框架元件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與模型
import com.example.demo.dao.AnswerDAO;
import com.example.demo.dao.QuizResultDAO;
import com.example.demo.entity.Answer;
import com.example.demo.entity.QuizResult;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * QuizHistoryDetailController：
 * 提供使用者的歷史測驗詳解 API
 *
 * 路徑：GET /api/quizHistoryDetail?userId=123
 * 回傳格式：JSON 陣列，每部影片為一筆，包含影片標題與多次作答紀錄
 * 每筆作答紀錄中包含每題詳解（正確與否、選項、解析、答題順序等）
 */
@RestController // 宣告為 REST 控制器，回傳 JSON 結構
@RequestMapping("/api") // 路徑前綴為 /api
public class QuizHistoryDetailController {

    @Autowired // 注入測驗結果 DAO
    private QuizResultDAO quizResultDAO;

    @Autowired // 注入答題紀錄 DAO
    private AnswerDAO answerDAO;

    /**
     * 根據使用者 ID，回傳其所有影片的測驗詳解紀錄（以影片分群）
     * @param userId 使用者 ID
     * @return JSON 字串，影片群組 + 每次作答紀錄 + 各題詳解
     */
    @GetMapping("/quizHistoryDetail")
    public String getQuizHistoryGrouped(@RequestParam("userId") int userId) {
        JsonArray videoArray = new JsonArray(); // 最終回傳的 JSON 陣列

        try {
            // 查詢所有該使用者的作答紀錄（每次測驗紀錄）
            List<QuizResult> results = quizResultDAO.getResultsByUser(userId);

            // 用 LinkedHashMap 保留插入順序，避免影片順序亂掉
            Map<String, JsonObject> videoMap = new LinkedHashMap<>();

            for (QuizResult result : results) {
                String videoId = result.getVideoId();
                String videoTitle = answerDAO.getVideoTitle(videoId); // 🔍 額外查影片標題

                // 如果此影片尚未建立資料物件，先建立一個外層物件
                if (!videoMap.containsKey(videoId)) {
                    JsonObject videoObj = new JsonObject();
                    videoObj.addProperty("videoId", videoId);
                    videoObj.addProperty("videoTitle", videoTitle);
                    videoObj.add("attempts", new JsonArray()); // 初始化 attempts 陣列
                    videoMap.put(videoId, videoObj);
                }

                // 查詢這次測驗（attemptId）對應的所有題目答題詳解
                List<Answer> answers = answerDAO.getAnswersByUserAndAttemptId(
                        userId, videoId, result.getAttemptId());

                JsonArray answerArr = new JsonArray(); // 每題詳解陣列

                for (int i = 0; i < answers.size(); i++) {
                    Answer a = answers.get(i);

                    JsonObject ans = new JsonObject();
                    ans.addProperty("question", a.getQuestion());                           // 題目內容
                    ans.addProperty("selectedAnswer", a.getOptionTextByIndex(a.getSelectedOption())); // 使用者選擇的選項文字
                    ans.addProperty("correctAnswer", a.getAnswer());                        // 正確答案文字
                    ans.addProperty("isCorrect", a.isCorrect());                            // 是否答對
                    ans.addProperty("explanation", a.getExplanation());                     // 解說
                    ans.addProperty("option1", a.getOption1());                             // 選項 A
                    ans.addProperty("option2", a.getOption2());                             // 選項 B
                    ans.addProperty("option3", a.getOption3());                             // 選項 C
                    ans.addProperty("option4", a.getOption4());                             // 選項 D
                    ans.addProperty("correctIndex", a.getAnswerIndex());                    // 正確選項索引
                    ans.addProperty("selectedIndex", a.getSelectedOption());                // 使用者選擇索引
                    ans.addProperty("difficulty", a.getDifficulty());                       // 題目難度
                    ans.addProperty("originalIndex", i);                                     // 題目在該次測驗中的原始順序
                    answerArr.add(ans);
                }

                // 建立單次測驗（attempt）的資訊物件
                JsonObject attemptObj = new JsonObject();
                attemptObj.addProperty("attemptId", result.getAttemptId());                 // 作答紀錄唯一 ID
                attemptObj.addProperty("submittedAt", result.getSubmittedAt().toString());  // 作答時間
                attemptObj.addProperty("total", result.getTotalQuestions());                // 題數
                attemptObj.addProperty("correct", result.getCorrectAnswers());              // 答對題數
                attemptObj.add("answers", answerArr);                                       // 答題詳解陣列

                // 加入到對應影片物件的 attempts 陣列中
                JsonObject videoObj = videoMap.get(videoId);
                videoObj.getAsJsonArray("attempts").add(attemptObj);
            }

            // 收集所有影片的作答資料（保持影片順序）
            videoMap.values().forEach(videoArray::add);

        } catch (Exception e) {
            // 發生錯誤時回傳錯誤訊息 JSON
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", "處理錯誤：" + e.getMessage());
            return error.toString();
        }

        // 回傳整包 JSON 陣列字串
        return videoArray.toString();
    }
}
