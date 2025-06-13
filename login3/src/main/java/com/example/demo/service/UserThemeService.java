package com.example.demo.service;

import com.example.demo.entity.UserTheme;
// import com.example.demo.entity.User; // 不再需要在這裡引入 User 實體，如果只處理 ID
// import com.example.demo.entity.Theme; // 不再需要在這裡引入 Theme 實體，如果只處理 ID
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
// import java.util.stream.Collectors; // 不再需要

@Service
public class UserThemeService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 移除這些導致循環依賴的注入！
    // @Autowired
    // private MemberService memberService;
    // @Autowired
    // private ThemeService themeService;

    // 自定義 RowMapper 來映射 ResultSet 到 UserTheme 對象
    // 這個 RowMapper 只處理 user_themes 表自身的欄位，以及 JOIN 過來的 theme 資訊
    private class UserThemeRowMapper implements RowMapper<UserTheme> {
        @Override
        public UserTheme mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserTheme userTheme = new UserTheme();
            userTheme.setId(rs.getLong("id"));
            userTheme.setPurchasedAt(rs.getTimestamp("purchased_at"));

            // 直接設定 User 和 Theme 的 ID，不在這裡注入 Service 查詢完整物件
            // 因為這個 RowMapper 是用於 `getUserPurchasedThemes`，該方法已經 JOIN 了 themes 表
            // 所以可以直接從 ResultSet 獲取 theme_name, display_name, price
            // User 對象在這裡只設定 ID 即可，完整 User 對象的查詢是 MemberService 的職責
            // (如果需要完整 User 對象，應該在 Controller 或調用層級透過 MemberService 查詢)

            // 為了兼容 UserTheme 實體中的 User 和 Theme 對象屬性，
            // 這裡可以創建一個簡單的 User 和 Theme 對象並只設定 ID
            // 如果需要完整的 User 和 Theme 資訊，則需要修改 UserTheme 實體或在更高層次進行組裝
            // 目前的 MemberController 已經在 `welcomePage` 中查詢了完整 `User` 和 `Theme` 列表。
            // 這裡 RowMapper 的目的主要是獲取 `user_themes` 和 `themes` 的關聯信息。
            com.example.demo.entity.User user = new com.example.demo.entity.User();
            user.setId(rs.getLong("user_id"));
            userTheme.setUser(user);

            com.example.demo.entity.Theme theme = new com.example.demo.entity.Theme();
            theme.setId(rs.getLong("theme_id"));
            // 從 JOIN 的結果中直接獲取 theme 的資訊
            theme.setThemeName(rs.getString("theme_name"));
            theme.setDisplayName(rs.getString("display_name"));
            theme.setPrice(rs.getInt("price"));
            userTheme.setTheme(theme);

            return userTheme;
        }
    }


    public List<UserTheme> getUserPurchasedThemes(Long userId) {
        String sql = "SELECT ut.id, ut.user_id, ut.theme_id, ut.purchased_at, t.theme_name, t.display_name, t.price " +
                     "FROM user_themes ut JOIN themes t ON ut.theme_id = t.id WHERE ut.user_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, new UserThemeRowMapper());
    }

    public boolean hasUserPurchasedTheme(Long userId, String themeName) {
        String sql = "SELECT COUNT(*) FROM user_themes ut " +
                     "JOIN themes t ON ut.theme_id = t.id " +
                     "WHERE ut.user_id = ? AND t.theme_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, themeName);
        return count != null && count > 0;
    }

    // 新增購買記錄
    public void addUserTheme(Long userId, Long themeId) {
        String sql = "INSERT INTO user_themes (user_id, theme_id, purchased_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, themeId, new Timestamp(System.currentTimeMillis()));
    }

    // 獲取用戶已購買的主題名稱列表，用於前端下拉選單
    public List<String> getPurchasedThemeNamesByUserId(Long userId) {
        String sql = "SELECT t.theme_name FROM user_themes ut " +
                     "JOIN themes t ON ut.theme_id = t.id WHERE ut.user_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    // 當刪除用戶時，刪除其所有購買的主題記錄
    // 這個方法會被 MemberService 呼叫，但 UserThemeService 不會反向依賴 MemberService
    public void deleteUserThemesByUserId(Long userId) {
        jdbcTemplate.update("DELETE FROM user_themes WHERE user_id = ?", userId);
    }
}