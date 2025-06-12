package com.example.demo.service;

import com.example.demo.entity.UserEntity; // <--- 確保引入 UserEntity
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.dao.EmptyResultDataAccessException; // 引入此異常，用於查詢不到結果的情況

import java.util.Arrays;
import java.util.List;
import java.util.Optional; // <--- 引入 Optional

@Service
public class MemberService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- 新增：定義所有可用的佈景主題列表 ---
    // 實際應用中，這個列表可以從資料庫、配置檔或專門的 ThemeService 獲取，而不是硬編碼
    // 與 MemberController 中的列表保持一致
    private static final List<String> AVAILABLE_THEMES = Arrays.asList("default", "dark", "blue");


    // Project1 原有的註冊方法，將被新的驗證碼註冊流程取代，但可保留用於其他情況
    public boolean register(String email, String password, String name) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM member WHERE email = ?", Integer.class, email);

        if (count != null && count > 0) {
            return false; // Email 已存在
        }

        // 密碼雜湊
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // --- 修改此處：新增 coin 和 theme 的初始值 ---
        jdbcTemplate.update("INSERT INTO member(email, password, name, coin, theme) VALUES (?, ?, ?, ?, ?)",
                email, hashedPassword, name, 0L, "default"); // 新增 coin=0L, theme='default'

        return true;
    }

    // 新增方法：處理驗證碼註冊流程中最終的用戶註冊
    public boolean registerUserAfterVerification(String email, String password, String name) {
        // 在這裡不需要再次檢查 email 是否存在，因為 VerificationService.generateAndSendCode
        // 已經檢查過，並且驗證碼流程確保了新用戶。
        // 但是，為了一致性，我們仍然可以檢查一次，或者簡化直接插入。
        // 這裡選擇直接插入，因為假定調用者已完成驗證碼和 email 唯一性檢查。

        // 密碼雜湊
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // --- 修改此處：新增 coin 和 theme 的初始值 ---
        int rowsAffected = jdbcTemplate.update("INSERT INTO member(email, password, name, coin, theme) VALUES (?, ?, ?, ?, ?)",
                email, hashedPassword, name, 0L, "default"); // 新增 coin=0L, theme='default'

        return rowsAffected > 0;
    }

    public boolean authenticate(String email, String password) {
        try {
            // 從數據庫獲取雜湊後的密碼
            String hashedPassword = jdbcTemplate.queryForObject(
                    "SELECT password FROM member WHERE email = ?", String.class, email);

            // 處理 Google 登入的特殊情況 (密碼為 "google")
            if ("google".equals(hashedPassword)) {
                // 如果是 Google 帳號，直接返回 true，因為其登入由 Google 驗證
                return true;
            }

            // 驗證輸入的密碼是否與雜湊後的密碼匹配
            return BCrypt.checkpw(password, hashedPassword);
        } catch (EmptyResultDataAccessException e) { // 使用更具體的異常捕獲
            // 沒有找到該 email 的用戶
            return false;
        } catch (Exception e) {
            // 其他異常，如多個結果等
            e.printStackTrace();
            return false;
        }
    }

    public String getNameByUsername(String email) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT name FROM member WHERE email = ?", String.class, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // --- 新增方法：根據 Email 獲取完整的 UserEntity ---
    public Optional<UserEntity> findUserByEmail(String email) {
        String sql = "SELECT id, email, password, name, coin, theme FROM member WHERE email = ?";
        try {
            // 使用 queryForObject 配合 RowMapper 將結果映射到 UserEntity
            // 這需要一個 RowMapper 來處理結果集到物件的映射
            UserEntity user = jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
                UserEntity userEntity = new UserEntity();
                userEntity.setId(rs.getLong("id"));
                userEntity.setEmail(rs.getString("email"));
                userEntity.setPassword(rs.getString("password"));
                userEntity.setName(rs.getString("name"));
                userEntity.setCoin(rs.getLong("coin")); // 讀取 coin 欄位 (BIGINT 對應 Long)
                userEntity.setTheme(rs.getString("theme")); // 讀取 theme 欄位
                return userEntity;
            });
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            // 如果沒有找到對應的用戶，返回空的 Optional
            return Optional.empty();
        } catch (Exception e) {
            // 處理其他可能的異常，例如資料庫連接問題等
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // --- 新增方法：更新用戶的佈景主題 ---
    public void updateThemeByEmail(String email, String newTheme) {
        String sql = "UPDATE member SET theme = ? WHERE email = ?";
        int rowsAffected = jdbcTemplate.update(sql, newTheme, email);
        if (rowsAffected == 0) {
            // 可以選擇拋出異常或記錄日誌，表示用戶不存在或更新失敗
            System.err.println("Failed to update theme for user: " + email + ". User not found or no change.");
            // 或者 throw new RuntimeException("User not found or theme update failed.");
        }
    }

    // --- 新增方法：獲取所有可用的佈景主題列表 (與 Controller 中的列表一致) ---
    public List<String> getAvailableThemes() {
        return AVAILABLE_THEMES;
    }


    public boolean deleteUserByEmail(String email) {
        int rows = jdbcTemplate.update("DELETE FROM member WHERE email = ?", email);
        return rows > 0;
    }

    public void updatePassword(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        jdbcTemplate.update("UPDATE member SET password = ? WHERE email = ?", hashedPassword, email);
    }
}