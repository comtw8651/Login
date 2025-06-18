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

    // 將 password 設置為可空，因為 Google 登入的用戶可能沒有本地密碼
    @Column(nullable = true) // <--- 修改這裡：允許為空
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long coin;

    @Column(nullable = false, name = "current_theme")
    private String currentTheme;

    // 新增字段：標記是否為 Google 連結的帳戶
    @Column(nullable = false) // 通常預設為 false
    private boolean googleConnected; // <--- 新增此欄位

    // 無參數建構子 (JPA 需要)
    public User() {
        this.coin = 100L; // 預設值
        this.currentTheme = "default"; // 預設值
        this.googleConnected = false; // 預設為 false
    }

    // 所有欄位的建構子 (可選，方便測試或初始化)
    public User(Long id, String email, String password, String name, Long coin, String currentTheme, boolean googleConnected) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.coin = coin;
        this.currentTheme = currentTheme;
        this.googleConnected = googleConnected;
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

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        this.currentTheme = currentTheme;
    }

    // 新增 googleConnected 的 Getter & Setter
    public boolean isGoogleConnected() {
        return googleConnected;
    }

    public void setGoogleConnected(boolean googleConnected) {
        this.googleConnected = googleConnected;
    }
}