package com.example.demo.config; // 請根據你的專案結構調整套件名稱

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 標示這是一個配置類
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置 /google-login 路徑的 CORS
        registry.addMapping("/google-login") // 只對 /google-login 這個路徑應用 CORS 規則
                .allowedOrigins("http://localhost:8080") // 允許來自這個來源的請求
                .allowedMethods("POST") // 允許 POST 方法
                .allowedHeaders("*") // 允許所有請求頭
                .allowCredentials(true) // 允許發送像 Cookie 這樣的認證信息
                .maxAge(3600); // 預檢請求的緩存時間，單位秒

        // 如果你的 /index, /login, /register 等頁面也是由後端提供服務，
        // 且前端有其他 JS 請求這些路徑，則可能需要更通用的規則。
        // 但對於 Google 登入的問題，主要關注 /google-login。
        // 例如，如果你希望所有來自 localhost:8080 的請求都能跨域：
        /*
        registry.addMapping("/**") // 允許所有路徑
                .allowedOrigins("http://localhost:8080") // 允許來自這個來源的請求
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允許所有常見的 HTTP 方法
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        */
    }
}