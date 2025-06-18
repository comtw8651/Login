package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.Theme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
    private ThemeService themeService;
    @Autowired
    private UserThemeService userThemeService;

    // Project1 原有的註冊方法，將被新的驗證碼註冊流程取代，但可保留用於其他情況
    @Transactional
    public boolean register(String email, String password, String name) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM member WHERE email = ?", Integer.class, email);

        if (count != null && count > 0) {
            return false; // Email 已存在
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 插入 member 表，新增 google_connected 欄位
        jdbcTemplate.update("INSERT INTO member(email, password, name, coin, current_theme, google_connected) VALUES (?, ?, ?, ?, ?, ?)",
                email, hashedPassword, name, 100L, "default", false); // <--- 這裡設置為 false

        Long userId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, email);
        Optional<Theme> defaultThemeOptional = themeService.findByThemeName("default");

        if (userId != null && defaultThemeOptional.isPresent()) {
            Theme defaultTheme = defaultThemeOptional.get();
            userThemeService.addUserTheme(userId, defaultTheme.getId());
        } else {
            System.err.println("Error: Default theme not found or user ID could not be retrieved during registration for email: " + email);
            throw new RuntimeException("Registration failed: Could not assign default theme or retrieve user ID.");
        }

        return true;
    }

    @Transactional
    public boolean registerUserAfterVerification(String email, String password, String name) {
        if (findUserByEmail(email).isPresent()) {
            return false; // Email 已存在
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 插入 member 表，新增 google_connected 欄位
        int rowsAffected = jdbcTemplate.update("INSERT INTO member(email, password, name, coin, current_theme, google_connected) VALUES (?, ?, ?, ?, ?, ?)",
                email, hashedPassword, name, 100L, "default", false); // <--- 這裡設置為 false

        if (rowsAffected > 0) {
            Long userId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, email);
            Optional<Theme> defaultThemeOptional = themeService.findByThemeName("default");

            if (userId != null && defaultThemeOptional.isPresent()) {
                Theme defaultTheme = defaultThemeOptional.get();
                userThemeService.addUserTheme(userId, defaultTheme.getId());
                return true;
            } else {
                System.err.println("Error: Default theme not found or user ID could not be retrieved during verification registration for email: " + email);
                throw new RuntimeException("Verification registration failed: Could not assign default theme or retrieve user ID.");
            }
        }
        return false;
    }

    // 新增或更新 Google 登入用戶的方法
    @Transactional
    public User saveGoogleUser(String email, String name) {
        Optional<User> existingUserOptional = findUserByEmail(email);

        User user;
        if (existingUserOptional.isPresent()) {
            user = existingUserOptional.get();
            // 如果是現有用戶，且不是 Google 連接的，可以更新為 Google 連接
            if (!user.isGoogleConnected()) {
                jdbcTemplate.update("UPDATE member SET google_connected = TRUE WHERE id = ?", user.getId());
                user.setGoogleConnected(true);
            }
            // 可以更新 name，如果 Google 傳回的名字更為準確
            if (!user.getName().equals(name)) { // 避免不必要的更新
                 jdbcTemplate.update("UPDATE member SET name = ? WHERE id = ?", name, user.getId());
                 user.setName(name);
            }
            System.out.println("🔁 Existing user logged in via Google: " + email);
        } else {
            // 新用戶 → 自動註冊
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(null); // Google 登入的用戶，本地沒有密碼，設置為 null
            user.setCoin(100L); // 預設金幣
            user.setCurrentTheme("default"); // 預設主題
            user.setGoogleConnected(true); // 標記為 Google 連接

            // 使用 JdbcTemplate 插入數據，並獲取生成的主鍵 ID
            // 注意：這裡假設你的 member 表 ID 是自增的
            jdbcTemplate.update("INSERT INTO member(email, password, name, coin, current_theme, google_connected) VALUES (?, ?, ?, ?, ?, ?)",
                    user.getEmail(), user.getPassword(), user.getName(), user.getCoin(), user.getCurrentTheme(), user.isGoogleConnected());

            // 獲取剛剛插入的用戶 ID
            Long userId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, email);
            user.setId(userId); // 設定 User 物件的 ID

            // 為新用戶新增 'default' 主題的購買記錄
            Optional<Theme> defaultThemeOptional = themeService.findByThemeName("default");
            if (userId != null && defaultThemeOptional.isPresent()) {
                Theme defaultTheme = defaultThemeOptional.get();
                userThemeService.addUserTheme(userId, defaultTheme.getId());
            } else {
                System.err.println("Error: Default theme not found or user ID could not be retrieved during Google registration for email: " + email);
                throw new RuntimeException("Google registration failed: Could not assign default theme or retrieve user ID.");
            }
            System.out.println("✅ New Google user registered: " + email);
        }
        return user;
    }


    public boolean authenticate(String email, String password) {
        try {
            // 首先嘗試獲取整個 User 對象，以便檢查 googleConnected 狀態
            Optional<User> userOptional = findUserByEmail(email);

            if (userOptional.isEmpty()) {
                return false; // Email 不存在
            }

            User user = userOptional.get();

            // 如果是用 Google 帳戶連接的，且使用者嘗試使用密碼登入
            // 這裡可以選擇不允許，或者要求他們透過 Google 登入
            if (user.isGoogleConnected()) {
                // 如果是 Google 帳戶，不允許傳統密碼登入，或者僅允許 Google 登入
                // 這裡的邏輯取決於你的設計：是強制 Google 登入，還是允許同時使用密碼
                // 為了簡化，如果設定了 googleConnected=true，則該方法不處理
                System.out.println("User " + email + " is Google-connected. Direct password authentication not handled here.");
                return false; // 不通過傳統方式驗證 Google 連接帳戶
            }

            // 對於非 Google 連接的帳戶，進行 BCrypt 密碼比對
            return BCrypt.checkpw(password, user.getPassword());

        } catch (Exception e) {
            System.err.println("Authentication error for email: " + email + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public String getNameByUsername(String email) {
        try {
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
    // 添加了 google_connected 欄位
    public Optional<User> findUserByEmail(String email) {
        String sql = "SELECT id, email, password, name, coin, current_theme, google_connected FROM member WHERE email = ?";
        try {
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
            userEntity.setPassword(rs.getString("password")); // 允許為 null
            userEntity.setName(rs.getString("name"));
            userEntity.setCoin(rs.getLong("coin"));
            userEntity.setCurrentTheme(rs.getString("current_theme"));
            userEntity.setGoogleConnected(rs.getBoolean("google_connected")); // <--- 新增這裡
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
        }
    }

    // --- 修改：更新用戶的佈景主題 ---
    @Transactional
    public void updateThemeByEmail(String email, String newTheme) {
        String sql = "UPDATE member SET current_theme = ? WHERE email = ?";
        int rowsAffected = jdbcTemplate.update(sql, newTheme, email);
        if (rowsAffected == 0) {
            System.err.println("Failed to update current_theme for user: " + email + ". User not found or no change.");
        }
    }

    @Transactional
    public boolean deleteUserByEmail(String email) {
        Optional<User> userOptional = findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            jdbcTemplate.update("DELETE FROM user_themes WHERE user_id = ?", user.getId());
            int rows = jdbcTemplate.update("DELETE FROM member WHERE email = ?", email);
            return rows > 0;
        }
        return false;
    }

    public void updatePassword(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        int rowsAffected = jdbcTemplate.update("UPDATE member SET password = ? WHERE email = ? AND google_connected = FALSE", hashedPassword, email); // 只更新非 Google 連接的用戶密碼
        if (rowsAffected == 0) {
             System.err.println("Failed to update password for user: " + email + ". User not found or is Google connected.");
        }
    }
}