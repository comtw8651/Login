package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "member")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // 用戶的貨幣餘額，使用 Long 對應資料庫的 BIGINT
    @Column(nullable = false) // 如果可以為空或有預設值，再調整
    private Long coin;

    // 用戶選擇的佈景主題名稱 (這個是我們主要使用的欄位)
    // 注意：這裡使用 name = "current_theme" 來明確指定資料庫欄位名稱
    // 以確保與 JdbcTemplate 的查詢一致性
    @Column(nullable = false, name = "current_theme")
    private String currentTheme;

    // 無參數建構子 (JPA 需要)
    public User() {
    }

    // 所有欄位的建構子 (可選，方便測試或初始化)
    public User(Long id, String email, String password, String name, Long coin, String currentTheme) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.coin = coin;
        this.currentTheme = currentTheme;
    }

    // --- Getter & Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCoin() {
        return coin;
    }

    public void setCoin(Long coin) {
        this.coin = coin;
    }

    // --- currentTheme 的 Getter & Setter (取代了原本的 getTheme/setTheme) ---
    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        this.currentTheme = currentTheme;
    }
}