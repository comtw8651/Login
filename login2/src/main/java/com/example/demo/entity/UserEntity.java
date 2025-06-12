package com.example.demo.entity;

import jakarta.persistence.*;
// import java.math.BigDecimal; // 不再需要引入 BigDecimal

@Entity
@Table(name = "member")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // --- 修改後的欄位 ---

    // 用戶的貨幣餘額，使用 Long 對應資料庫的 BIGINT
    // 例如：如果 100.50 美元，您可以將其儲存為 10050 (美分)
    // 或者如果是台幣，直接存入整數金額，例如 500 元就存 500
    @Column(nullable = false) // 如果可以為空或有預設值，再調整
    private Long coin;

    // 用戶選擇的佈景主題名稱
    @Column(nullable = false) // 根據您的業務邏輯，如果可以沒有主題可以設定為 true
    private String theme;

    // --- Getter & Setter (保留原本的，並為新欄位新增) ---

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

    // --- 新增 coin 的 Getter & Setter ---
    public Long getCoin() {
        return coin;
    }

    public void setCoin(Long coin) {
        this.coin = coin;
    }

    // --- 新增 theme 的 Getter & Setter ---
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}