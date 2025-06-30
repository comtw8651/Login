package com.example.demo.controller;

// 匯入 Gson 用於組成 JSON 回傳格式
import com.google.gson.JsonObject;

// 匯入 Spring Boot 相關註解與類別
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入資料存取物件 DAO
import com.example.demo.dao.UserDAO;

/**
 * GetExpController：回傳指定使用者的經驗值查詢 API
 * 使用方式：
 * 請求：GET /api/getExp?userId=123
 * 回應：{ "exp": 25 }
 */
@RestController // 設為 REST 控制器，直接回傳 JSON 格式的資料
@RequestMapping("/api") // 統一設定 API 路徑前綴
public class GetExpController {

    @Autowired // 由 Spring 自動注入 UserDAO 實例
    private UserDAO userDAO;

    /**
     * 取得指定使用者的經驗值
     * @param userId 使用者 ID（從 URL 查詢參數取得）
     * @return 回傳 JSON 字串，例如：{ "exp": 25 } 或錯誤訊息
     */
    @GetMapping("/getExp")
    public String getExp(@RequestParam("userId") int userId) {
        try {
            // 從資料庫查詢使用者目前的經驗值
            int exp = userDAO.getExp(userId);

            // 組成回傳用 JSON 格式
            JsonObject json = new JsonObject();
            json.addProperty("exp", exp); // 加入 exp 欄位
            return json.toString();       // 回傳 JSON 字串

        } catch (Exception e) {
            // 若發生錯誤，回傳錯誤訊息（用 JSON 格式包裝）
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
