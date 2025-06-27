package com.example.demo.controller;

// 匯入 Spring 的自動注入與 REST 控制器相關註解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入資料存取與模型類別
import com.example.demo.dao.AnswerDAO;
import com.example.demo.entity.AnswerDetailDto;
import com.example.demo.entity.Answer;

import java.util.ArrayList;
import java.util.List;

/**
 * AnswerDetailController：查詢單次作答詳解的 API 控制器
 * 功能：回傳某使用者對某影片一次測驗的所有作答詳解資料（選項、答案、解析等）
 * 範例路徑：GET /api/answerDetail?userId=123&videoId=abc123&attemptId=1750xxxxxx
 */
@RestController // 表示這是一個 REST 控制器，會直接回傳 JSON 結果
@RequestMapping("/api") // 設定所有方法的基礎路徑為 /api
public class AnswerDetailController {

    @Autowired // 由 Spring 自動注入 AnswerDAO 實例
    private AnswerDAO answerDAO;

    /**
     * 取得某使用者對特定影片在某次測驗的所有答題詳解
     * @param userId 使用者 ID
     * @param videoId 影片 ID
     * @param attemptId 作答紀錄的唯一識別碼
     * @return 該次測驗的所有答題詳解清單
     */
    @GetMapping("/answerDetail") // 定義 GET 請求路徑為 /api/answerDetail
    public List<AnswerDetailDto> getAnswerDetail(@RequestParam("userId") int userId,
                                                 @RequestParam("videoId") String videoId,
                                                 @RequestParam("attemptId") long attemptId) {
        List<AnswerDetailDto> result = new ArrayList<>();

        // ✅ 查詢該使用者針對某影片、某次作答的所有紀錄（每筆代表一道題）
        List<Answer> records = answerDAO.getAnswersByUserAndAttemptId(userId, videoId, attemptId);

        // 對每筆作答記錄進行處理
        for (Answer record : records) {
            AnswerDetailDto dto = new AnswerDetailDto(); // 建立回傳用的資料傳輸物件

            // 將題目與四個選項寫入 DTO
            dto.setQuestion(record.getQuestion());
            dto.setOption1(record.getOption1());
            dto.setOption2(record.getOption2());
            dto.setOption3(record.getOption3());
            dto.setOption4(record.getOption4());

            // 設定正確答案索引（0~3）
            dto.setCorrectIndex(record.getAnswerIndex());

            // 設定使用者選擇的選項索引
            dto.setSelectedIndex(record.getSelectedOption());

            // 設定題目來源（本地/GPT）
            dto.setSource(record.getSource());

            // 設定解析文字
            dto.setExplanation(record.getExplanation());

            // 安全處理：組成選項文字陣列（避免 null）
            List<String> options = List.of(
                    record.getOption1() != null ? record.getOption1() : "",
                    record.getOption2() != null ? record.getOption2() : "",
                    record.getOption3() != null ? record.getOption3() : "",
                    record.getOption4() != null ? record.getOption4() : ""
            );

            Integer answerIndex = record.getAnswerIndex();       // 正確答案索引
            Integer selectedIndex = record.getSelectedOption();  // 使用者選擇索引

            // 根據索引取得正確答案文字（超出範圍時給空字串）
            String correctText = (answerIndex != null && answerIndex >= 0 && answerIndex < options.size())
                    ? options.get(answerIndex) : "";

            // 根據索引取得使用者所選的選項文字（超出範圍時給空字串）
            String selectedText = (selectedIndex != null && selectedIndex >= 0 && selectedIndex < options.size())
                    ? options.get(selectedIndex) : "";

            // 寫入選項文字（用於前端顯示）
            dto.setCorrect(correctText);
            dto.setSelected(selectedText);

            // 加入結果清單
            result.add(dto);
        }

        return result; // 回傳所有題目的詳解資料
    }
}
