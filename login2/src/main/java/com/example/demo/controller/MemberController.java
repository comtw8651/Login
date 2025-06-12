package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.ResetPasswordForm;
import com.example.demo.entity.UserForm;
import com.example.demo.entity.UserEntity; // <--- 確保引入 UserEntity
import com.example.demo.service.MemberService;
import com.example.demo.service.ResetPasswordService;
import com.example.demo.service.VerificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap; // <--- 引入 HashMap
import java.util.List;   // <--- 引入 List
import java.util.Map;    // <--- 引入 Map
import java.util.Arrays; // <--- 引入 Arrays (用於示範可用主題列表)


@Controller
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService; // 您現有的 MemberService

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private ResetPasswordService resetPasswordService;

    // --- 新增：定義所有可用的佈景主題列表 ---
    // 實際應用中，這個列表可以從資料庫、配置檔或專門的 ThemeService 獲取，而不是硬編碼
    private static final List<String> AVAILABLE_THEMES = Arrays.asList("default", "dark", "blue"); // 確保與您的 CSS class 對應

    // 首頁處理 (保持不變，因為 /welcome 是真正的會員首頁)
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            // 這個 'welcome' 可能是登入後的一個過渡頁面，
            // 真正的會員首頁處理邏輯應該在 /member 或 /welcome 裡
            String name = memberService.getNameByUsername(email);
            model.addAttribute("name", name);
            return "welcome"; // 或者直接導向 /member
        }
        return "redirect:/login";
    }

    // 顯示登入頁面
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "login";
    }

    // 處理傳統登入
    @PostMapping("/login")
    public String doLogin(@ModelAttribute("userForm") UserForm userForm,
                          Model model,
                          HttpSession session) {

        String email = userForm.getEmail();
        String password = userForm.getPasswd();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error1", "帳號與密碼不得為空");
            logger.warn("Login attempt with empty email or password.");
            return "login";
        }

        if (memberService.authenticate(email, password)) {
            session.setAttribute("email", email);
            String name = memberService.getNameByUsername(email);
            session.setAttribute("name", name);
            logger.info("User {} logged in successfully.", email);
            return "redirect:/welcome";
        } else {
            model.addAttribute("error", "登入失敗，請檢查Email或密碼");
            logger.warn("Login failed for email: {}", email);
            return "login";
        }
    }

    // 顯示註冊頁面 (請求驗證碼)
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "register";
    }

    // 處理發送驗證碼請求
    @PostMapping("/register/request-code")
    public String requestCode(@ModelAttribute("userForm") @Valid UserForm userForm,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {
        if (bindingResult.hasErrors() && bindingResult.getFieldError("email") != null) {
            model.addAttribute("error", bindingResult.getFieldError("email").getDefaultMessage());
            logger.warn("Register request with invalid email: {}", userForm.getEmail());
            return "register";
        }

        String email = userForm.getEmail();
        // getNameByUsername 其實是判斷用戶是否存在的一種方式，
        // 如果您希望直接判斷用戶實體是否存在，可以考慮在 MemberService 中新增 findUserByEmail 方法
        if (memberService.getNameByUsername(email) != null) { // 判斷用戶是否已存在
            model.addAttribute("error", "該Email已經被註冊。");
            logger.warn("Register request for already registered email: {}", email);
            return "register";
        }

        verificationService.generateAndSendCode(email);
        session.setAttribute("emailForVerification", email);
        logger.info("Verification code requested for email: {}", email);
        return "redirect:/verify";
    }

    // 顯示驗證碼輸入頁面
    @GetMapping("/verify")
    public String showVerifyPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("emailForVerification");
        if (email == null) {
            logger.warn("Access to verify page without email in session.");
            return "redirect:/register";
        }
        model.addAttribute("email", email);
        model.addAttribute("userForm", new UserForm());
        return "verify";
    }

    // 處理驗證碼並完成註冊
    @PostMapping("/register/verify")
    public String verifyCodeAndRegister(@ModelAttribute("userForm") @Valid UserForm userForm,
                                        BindingResult bindingResult,
                                        @RequestParam String email,
                                        @RequestParam String code,
                                        HttpSession session,
                                        Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "請檢查所有欄位是否正確填寫。";
            if (bindingResult.getFieldError("passwd") != null) {
                errorMessage = bindingResult.getFieldError("passwd").getDefaultMessage();
            } else if (bindingResult.getFieldError("name") != null) {
                errorMessage = bindingResult.getFieldError("name").getDefaultMessage();
            }
            model.addAttribute("error", errorMessage);
            model.addAttribute("email", email);
            logger.warn("Verification failed due to form errors for email {}: {}", email, bindingResult.getAllErrors());
            return "verify";
        }


        if (verificationService.verifyCode(email, code)) {
            // 註冊時，需要設定 coin 的初始值和預設 theme
            // registerUserAfterVerification 方法可能需要修改以接受這些參數
            // 或者在 MemberService 內部設定預設值
            boolean registered = memberService.registerUserAfterVerification(email, userForm.getPasswd(), userForm.getName());
            if (registered) {
                verificationService.deleteVerificationCode(email);
                session.removeAttribute("emailForVerification");
                model.addAttribute("message", "註冊成功，請登入！");
                logger.info("User {} registered successfully.", email);
                return "redirect:/login?registered";
            } else {
                model.addAttribute("error", "註冊失敗，請稍後再試。");
                model.addAttribute("email", email);
                logger.error("User registration failed after verification for email {}.", email);
                return "verify";
            }
        } else {
            model.addAttribute("error", "驗證碼錯誤或已過期。");
            model.addAttribute("email", email);
            logger.warn("Verification code invalid or expired for email {}.", email);
            return "verify";
        }
    }


    // 登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String email = (String) session.getAttribute("email");
        session.invalidate();
        logger.info("User {} logged out.", email);
        return "redirect:/login?logout";
    }

    // 刪除帳號
    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session) {
        String email = (String) session.getAttribute("email");

        if (email != null && memberService.deleteUserByEmail(email)) {
            session.invalidate();
            logger.info("Account {} deleted successfully.", email);
            return "redirect:/login?deleted";
        } else {
            logger.error("Failed to delete account for email {}.", email);
            return "redirect:/login?error";
        }
    }

    // 顯示修改密碼頁面
    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "change-password";
    }

    // 處理修改密碼
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model) {

        String email = (String) session.getAttribute("email");

        if (email == null) {
            logger.warn("Unauthorized access to change password page.");
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "新密碼與確認密碼不一致");
            logger.warn("Change password failed for {}: new password and confirm password mismatch.", email);
            return "change-password";
        }

        if (!memberService.authenticate(email, currentPassword)) {
            model.addAttribute("error", "原密碼錯誤");
            logger.warn("Change password failed for {}: current password incorrect.", email);
            return "change-password";
        }

        memberService.updatePassword(email, newPassword);
        model.addAttribute("success", "密碼已成功修改");
        logger.info("Password successfully changed for user {}.", email);
        return "change-password";
    }

    // --- 修改 /welcome 端點，使其作為會員首頁，顯示 coin 和 theme ---
    @GetMapping("/welcome") // 這個 /welcome 端點將會是會員首頁
    public String welcomePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            logger.warn("Access to welcome page without active session. Redirecting to login.");
            return "redirect:/login";
        }

        try {
            // 從 memberService 獲取完整的 UserEntity
            // 如果用戶不存在，findUserByEmail 已經返回 Optional.empty()
            // 這裡使用 orElseThrow，如果 Optional 為空就拋出異常
            UserEntity currentUser = memberService.findUserByEmail(email)
                                                  .orElseThrow(() -> new RuntimeException("User not found for email: " + email)); // <--- 使用 orElseThrow

            // 如果能走到這裡，說明 currentUser 肯定不為 null

            // 將用戶的姓名、貨幣和主題添加到 Model
            model.addAttribute("name", currentUser.getName());
            model.addAttribute("userCoin", currentUser.getCoin()); // 貨幣餘額
            model.addAttribute("currentTheme", currentUser.getTheme()); // 用戶當前主題
            model.addAttribute("availableThemes", memberService.getAvailableThemes()); // 所有可選主題列表 (從 MemberService 獲取)

            logger.info("User {} accessed welcome page. Coin: {}, Theme: {}.",
                        email, currentUser.getCoin(), currentUser.getTheme());

        } catch (RuntimeException e) { // 捕獲前面拋出的 RuntimeException
            logger.error("Error loading user data for {}: {}", email, e.getMessage());
            session.invalidate(); // 避免無效會話持續
            // 可以根據錯誤類型給出不同的重導向或錯誤訊息
            if (e.getMessage().contains("User not found")) {
                return "redirect:/login?error=userNotFound";
            }
            model.addAttribute("error", "無法載入您的資料，請稍後再試。");
            return "error_page"; // 導向一個通用錯誤頁面
        }

        return "welcome"; // 返回 welcome.html (您的會員首頁)
    }

    // --- 新增：處理前端 AJAX 請求，保存用戶選擇的佈景主題 ---
    @PostMapping("/saveTheme")
    @ResponseBody // 表示此方法直接返回數據，而不是視圖名稱
    public Map<String, String> saveTheme(@RequestBody Map<String, String> payload,
                                         HttpSession session) { // 從 Session 獲取用戶 Email

        String email = (String) session.getAttribute("email");
        if (email == null) {
            logger.warn("Unauthorized attempt to save theme: no active session.");
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "請先登入。");
            return response;
        }

        String selectedTheme = payload.get("theme");
        if (selectedTheme == null || selectedTheme.isEmpty()) {
            logger.warn("Save theme request from {} with missing theme name.", email);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "主題名稱不能為空。");
            return response;
        }

        // 檢查 selectedTheme 是否在可用的主題列表中，增加安全性
        if (!AVAILABLE_THEMES.contains(selectedTheme)) {
            logger.warn("Save theme request from {} with invalid theme name: {}", email, selectedTheme);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "無效的佈景主題。");
            return response;
        }

        try {
            // 調用 MemberService 更新用戶的主題
            // 您需要在 MemberService 中新增一個方法，例如 updateThemeByEmail(String email, String theme)
            memberService.updateThemeByEmail(email, selectedTheme);
            logger.info("User {} successfully updated theme to {}.", email, selectedTheme);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "佈景主題已成功更新。");
            return response;
        } catch (Exception e) {
            logger.error("Failed to save theme for user {}: {}", email, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "更新佈景主題失敗，請稍後再試。");
            return response;
        }
    }

    // --- 忘記密碼功能 (保持不變) ---
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("email", "");
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        Model model,
                                        HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        logger.info("Received forgot password request for email: {} from base URL: {}", email, baseUrl);

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email 不得為空");
            logger.warn("Forgot password request with empty email.");
            return "forgot-password";
        }

        boolean success = resetPasswordService.generateAndSendResetToken(email, baseUrl);
        if (success) {
            model.addAttribute("message", "重設密碼連結已發送到您的Email，請檢查您的信箱 (包含垃圾郵件)。");
            logger.info("Reset password link sent to email: {}", email);
        } else {
            model.addAttribute("message", "重設密碼連結已發送到您的Email，請檢查您的信箱 (包含垃圾郵件)。");
            logger.warn("Failed to generate and send reset token for email: {}. (Email might not exist or other issue)", email);
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token,
                                        Model model) {
        logger.info("Accessing reset-password page with token: {}", token);
        String email = resetPasswordService.validateToken(token);
        if (email != null) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setToken(token);
            model.addAttribute("resetPasswordForm", form);
            logger.info("Valid token received. Displaying reset password form for email associated with token.");
            return "reset-password";
        } else {
            model.addAttribute("error", "無效或已過期的重設密碼連結。請重新申請。");
            logger.warn("Invalid or expired reset password token received: {}. Redirecting to login.", token);
            return "redirect:/login";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@ModelAttribute("resetPasswordForm") @Valid ResetPasswordForm resetPasswordForm,
                                       BindingResult bindingResult,
                                       Model model) {
        logger.info("Processing reset password request for token: {}", resetPasswordForm.getToken());

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError("newPassword").getDefaultMessage());
            model.addAttribute("token", resetPasswordForm.getToken());
            return "reset-password";
        }

        if (!resetPasswordForm.getNewPassword().equals(resetPasswordForm.getConfirmPassword())) {
            model.addAttribute("error", "新密碼與確認密碼不一致");
            model.addAttribute("token", resetPasswordForm.getToken());
            return "reset-password";
        }

        boolean success = resetPasswordService.resetPassword(resetPasswordForm.getToken(), resetPasswordForm.getNewPassword());
        if (success) {
            model.addAttribute("message", "密碼已成功重設，請使用新密碼登入。");
            logger.info("Password successfully reset for token: {}", resetPasswordForm.getToken());
            return "redirect:/login?passwordReset";
        } else {
            model.addAttribute("error", "重設密碼失敗或連結已失效，請重新申請。");
            model.addAttribute("token", resetPasswordForm.getToken());
            logger.error("Failed to reset password for token {}. Invalid or expired token, or database error.", resetPasswordForm.getToken());
            return "reset-password";
        }
    }
}