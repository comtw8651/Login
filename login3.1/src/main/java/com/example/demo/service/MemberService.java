package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.Theme; // 引入 Theme 實體
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper; // 引入 RowMapper
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeService themeService; // 注入 ThemeService
    @Autowired
    private UserThemeService userThemeService; // 注入 UserThemeService

    // Project1 原有的註冊方法，將被新的驗證碼註冊流程取代，但可保留用於其他情況
    @Transactional // 確保事務完整性
    public boolean register(String email, String password, String name) {
        // 修改 deprecated 的 queryForObject 寫法
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM member WHERE email = ?", Integer.class, email);

        if (count != null && count > 0) {
            return false; // Email 已存在
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 插入 member 表
        jdbcTemplate.update("INSERT INTO member(email, password, name, coin, current_theme) VALUES (?, ?, ?, ?, ?)",
                email, hashedPassword, name, 100L, "default"); // 預設 100 金幣, 預設主題 'default'

        // 為新用戶新增 'default' 主題的購買記錄
        // 首先需要獲取剛剛插入的用戶的 ID
        // 修改 deprecated 的 queryForObject 寫法
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, email);
        Optional<Theme> defaultThemeOptional = themeService.findByThemeName("default");

        if (userId != null && defaultThemeOptional.isPresent()) {
            Theme defaultTheme = defaultThemeOptional.get();
            // 在 user_themes 表中添加記錄
            userThemeService.addUserTheme(userId, defaultTheme.getId());
        } else {
            // 處理預設主題不存在或用戶ID獲取失敗的情況
            // 在實際應用中，這裡可能需要拋出異常或回滾事務
            // 為了保持 register 方法返回 boolean，這裡也返回 false
            System.err.println("Error: Default theme not found or user ID could not be retrieved during registration for email: " + email);
            throw new RuntimeException("Registration failed: Could not assign default theme or retrieve user ID."); // 更明確地拋出異常
        }

        return true;
    }

    // 新增方法：處理驗證碼註冊流程中最終的用戶註冊
    @Transactional // 確保事務完整性
    public boolean registerUserAfterVerification(String email, String password, String name) {
        // 通常在驗證碼流程中已經確認過 email 唯一性，這裡直接插入。
        // 如果想更嚴謹，也可以再次檢查。
        if (findUserByEmail(email).isPresent()) {
            return false; // Email 已存在
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        int rowsAffected = jdbcTemplate.update("INSERT INTO member(email, password, name, coin, current_theme) VALUES (?, ?, ?, ?, ?)",
                email, hashedPassword, name, 100L, "default"); // 預設 100 金幣, 預設主題 'default'

        if (rowsAffected > 0) {
            // 為新用戶新增 'default' 主題的購買記錄
            // 修改 deprecated 的 queryForObject 寫法
            Long userId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, email);
            Optional<Theme> defaultThemeOptional = themeService.findByThemeName("default");

            if (userId != null && defaultThemeOptional.isPresent()) {
                Theme defaultTheme = defaultThemeOptional.get();
                userThemeService.addUserTheme(userId, defaultTheme.getId());
                return true;
            } else {
                System.err.println("Error: Default theme not found or user ID could not be retrieved during verification registration for email: " + email);
                // 這裡可能需要回滾，因為用戶數據已插入但主題關聯失敗
                // 為了保持 registerUserAfterVerification 方法返回 boolean，這裡也返回 false
                throw new RuntimeException("Verification registration failed: Could not assign default theme or retrieve user ID."); // 更明確地拋出異常
            }
        }
        return false;
    }

    public boolean authenticate(String email, String password) {
        try {
            // 修改 deprecated 的 queryForObject 寫法
            String hashedPassword = jdbcTemplate.queryForObject(
                    "SELECT password FROM member WHERE email = ?", String.class, email);

            if ("google".equals(hashedPassword)) { // 這裡應該更嚴謹地處理 Google 登入，可能不存儲實際密碼
                return true;
            }

            return BCrypt.checkpw(password, hashedPassword);
        } catch (EmptyResultDataAccessException e) {
            return false; // Email 不存在
        } catch (Exception e) {
            // 記錄所有其他異常，幫助除錯
            System.err.println("Authentication error for email: " + email + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getNameByUsername(String email) {
        try {
            // 修改 deprecated 的 queryForObject 寫法
            return jdbcTemplate.queryForObject(
                "SELECT name FROM member WHERE email = ?", String.class, email);
        } catch (EmptyResultDataAccessException e) {
            return null; // Email 不存在
        } catch (Exception e) {
            System.err.println("Error getting name for email: " + email + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- 修改：根據 Email 獲取完整的 UserEntity ---
    // 更改了欄位名稱 'theme' 為 'current_theme' 以匹配資料庫和 User 實體
    public Optional<User> findUserByEmail(String email) {
        String sql = "SELECT id, email, password, name, coin, current_theme FROM member WHERE email = ?";
        try {
            // 修改 deprecated 的 queryForObject 寫法
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // 用戶不存在
        } catch (Exception e) {
            System.err.println("Error finding user by email: " + email + " - " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // 自定義 RowMapper 來映射 ResultSet 到 User 對象
    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User userEntity = new User();
            userEntity.setId(rs.getLong("id"));
            userEntity.setEmail(rs.getString("email"));
            userEntity.setPassword(rs.getString("password"));
            userEntity.setName(rs.getString("name"));
            userEntity.setCoin(rs.getLong("coin"));
            userEntity.setCurrentTheme(rs.getString("current_theme")); // 注意這裡與 User 實體中的字段匹配
            return userEntity;
        }
    }


    // --- 新增方法：更新用戶的金幣 ---
    @Transactional
    public void updateUserCoin(User user) {
        String sql = "UPDATE member SET coin = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, user.getCoin(), user.getId());
        if (rowsAffected == 0) {
            System.err.println("Failed to update coin for user ID: " + user.getId() + ". User not found.");
            // 這裡可以考慮拋出一個特定的異常，例如 UserNotFoundException
        }
    }

    // --- 修改：更新用戶的佈景主題 ---
    // 這裡更新的是 member 表中的 current_theme 欄位
    @Transactional
    public void updateThemeByEmail(String email, String newTheme) {
        String sql = "UPDATE member SET current_theme = ? WHERE email = ?";
        int rowsAffected = jdbcTemplate.update(sql, newTheme, email);
        if (rowsAffected == 0) {
            System.err.println("Failed to update current_theme for user: " + email + ". User not found or no change.");
            // 這裡可以考慮拋出一個特定的異常
        }
    }

    @Transactional // 確保事務完整性
    public boolean deleteUserByEmail(String email) {
        Optional<User> userOptional = findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 先刪除 user_themes 關聯表中的紀錄，因為它有外鍵約束
            jdbcTemplate.update("DELETE FROM user_themes WHERE user_id = ?", user.getId());
            // 再刪除 member 表中的用戶紀錄
            int rows = jdbcTemplate.update("DELETE FROM member WHERE email = ?", email);
            return rows > 0;
        }
        return false;
    }

    public void updatePassword(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        int rowsAffected = jdbcTemplate.update("UPDATE member SET password = ? WHERE email = ?", hashedPassword, email);
        if (rowsAffected == 0) {
             System.err.println("Failed to update password for user: " + email + ". User not found.");
             // 這裡可以考慮拋出一個特定的異常
        }
    }
}