package com.example.demo.controller;

// 匯入 JSON 處理相關套件（Jackson）
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// 匯入 Spring 框架中的依賴注入與 REST 控制器相關註解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與資料模型
import com.example.demo.dao.AnswerDAO;
import com.example.demo.entity.AttemptGroup;
import com.example.demo.entity.AnswerGroupDTO;

import java.util.*;

/**
 * AnswerGroupedByAttemptAllController：
 * 查詢指定使用者所有影片的作答歷史記錄（依影片分群、內含各次作答記錄）
 * 路徑範例：GET /api/answerGroupAll?userId=1
 * 回傳格式：JSON 字串，內容為影片清單，每部影片含多次作答 attempt
 */
@RestController // 表示這是一個 RESTful 控制器，方法回傳值會直接轉成 JSON
@RequestMapping("/api") // 設定路徑前綴為 /api
public class AnswerGroupedByAttemptAllController {

    @Autowired // 由 Spring 自動注入 DAO 實例（連接資料庫用）
    private AnswerDAO answerDAO;

    /**
     * 取得某使用者針對所有影片的作答歷史紀錄（依影片與 attempt 群組）
     * @param userId 使用者 ID
     * @return JSON 字串，內含影片 ID、影片標題與多筆作答群組
     */
    @GetMapping("/answerGroupAll")
    public String getGroupedAnswersByUser(@RequestParam("userId") int userId) {
        List<AnswerGroupDTO> result = new ArrayList<>(); // 回傳用的 DTO 清單

        try {
            // 取得原始資料：Map<videoId, List<AttemptGroup>>
            // 每部影片對應多次作答記錄
            Map<String, List<AttemptGroup>> rawData = answerDAO.getAnswersGroupedByUserAcrossVideos(userId);

            // 將每個影片的作答資料轉換為 DTO 格式
            for (Map.Entry<String, List<AttemptGroup>> entry : rawData.entrySet()) {
                AnswerGroupDTO dto = new AnswerGroupDTO();

                dto.setVideoId(entry.getKey()); // 設定影片 ID
                dto.setVideoTitle(answerDAO.getVideoTitle(entry.getKey())); // 查詢並設定影片標題
                dto.setAttempts(entry.getValue()); // 設定該影片所有作答紀錄（attempts）

                result.add(dto); // 加入回傳清單
            }

            // ✅ 設定 Jackson 的 ObjectMapper，支援 LocalDateTime 正確序列化
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // 註冊 Java 8 時間模組
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 不寫成 timestamp 格式

            return mapper.writeValueAsString(result); // 轉換為 JSON 字串並回傳

        } catch (Exception e) {
            // 發生例外時回傳錯誤訊息 JSON
            e.printStackTrace();
            return "{\"error\": \"伺服器錯誤：" + e.getMessage() + "\"}";
        }
    }
}
