package com.example.demo.controller;

// 匯入 Google Gson 用於處理 JSON 字串
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// 匯入 Spring 框架相關註解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO
import com.example.demo.dao.UserDAO;

/**
 * ExpController：使用者經驗值更新 API 控制器
 * 功能：根據來源（看影片、答題等）更新使用者經驗值
 * 路徑：POST /api/exp
 * 接收資料格式為 JSON，例如：
 * {
 *     "userId": 123,
 *     "source": "watch",  // 來源（自定義字串：watch、quiz...）
 *     "exp": 10           // 要累加的經驗值
 * }
 */
@RestController // 表示這是一個 RESTful 控制器，會回傳純文字或 JSON 結果
@RequestMapping("/api") // 設定此控制器路徑前綴為 /api
public class ExpController {

    @Autowired // 自動注入 UserDAO，操作使用者資料表
    private UserDAO userDAO;

    /**
     * 更新經驗值的 POST API，接收 JSON 格式內容
     * @param body 請求的 JSON 字串
     * @return 回傳更新結果文字（成功或錯誤訊息）
     */
    @PostMapping("/exp")
    public String updateExp(@RequestBody String body) {
        try {
            // 將字串轉為 JSON 物件
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            // 檢查必要欄位是否存在
            if (!json.has("userId") || !json.has("source") || !json.has("exp")) {
                return "❌ 錯誤：請求資料不完整或不是有效的 JSON 格式";
            }

            // 解析資料
            int userId = json.get("userId").getAsInt();      // 使用者 ID
            String source = json.get("source").getAsString(); // 經驗值來源（如 watch/quiz）
            int exp = json.get("exp").getAsInt();            // 經驗值數量

            // 呼叫 DAO 更新使用者經驗值
            userDAO.addExp(userId, exp);

            // 成功回應
            return "✅ 成功累加經驗值：" + exp + " 點（來源：" + source + "）";

        } catch (Exception e) {
            // 發生錯誤時回傳例外訊息
            e.printStackTrace();
            return "🚫 發生錯誤：" + e.getMessage();
        }
    }
}
