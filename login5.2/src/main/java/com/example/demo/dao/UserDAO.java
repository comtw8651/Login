package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * UserDAO：使用者資料存取層
 * 功能包括經驗值管理、使用者驗證、新增使用者等
 */
@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ✅ 增加使用者經驗值
     *
     * @param userId 使用者的 ID
     * @param exp    要增加的經驗值數量（可正可負）
     * 執行結果：更新 users 資料表中對應使用者的 exp 欄位
     */
    public void addExp(int userId, int exp) {
        String sql = "UPDATE users SET exp = exp + ? WHERE id = ?";
        jdbcTemplate.update(sql, exp, userId);
    }

    /**
     * ✅ 查詢指定使用者的經驗值
     *
     * @param userId 使用者的 ID
     * @return 使用者目前的經驗值（int），若查無資料則回傳 0（不會拋例外）
     */
    public int getExp(int userId) {
        String sql = "SELECT exp FROM users WHERE id = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }

    /**
     * ✅ 檢查使用者是否存在
     *
     * @param userId 使用者的 ID
     * @return true：表示使用者存在；false：不存在或資料錯誤
     * 使用 COUNT(*) 判斷是否至少有一筆記錄
     */
    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    /**
     * ✅ 新增使用者（指定 ID、帳號、密碼，初始經驗值為 0）
     *
     * @param userId   使用者 ID（可為自訂或資料表主鍵）
     * @param username 使用者帳號
     * @param password 使用者密碼（建議實際應用中應加密處理）
     * 注意：此方法不會檢查 ID 重複，需在呼叫前先用 userExists() 檢查
     */
    public void insertUser(int userId, String username, String password) {
        String sql = "INSERT INTO users (id, username, password, exp) VALUES (?, ?, ?, 0)";
        jdbcTemplate.update(sql, userId, username, password);
    }
}
