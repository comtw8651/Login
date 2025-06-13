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
import com.example.demo.entity.User;
import com.example.demo.entity.Theme; // 引入 Theme 實體

import com.example.demo.service.MemberService;
import com.example.demo.service.ResetPasswordService;
import com.example.demo.service.VerificationService;
import com.example.demo.service.ThemeService;
import com.example.demo.service.UserThemeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // 引入 Optional

@Controller
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private ResetPasswordService resetPasswordService;

    @Autowired
    private ThemeService themeService;     // <--- 新增注入
    @Autowired
    private UserThemeService userThemeService; // <--- 新增注入

    // 首頁處理 (保持不變)
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            String name = memberService.getNameByUsername(email);
            model.addAttribute("name", name);
            return "welcome";
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
        if (memberService.findUserByEmail(email).isPresent()) {
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
    @GetMapping("/welcome")
    public String welcomePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            logger.warn("Access to welcome page without active session. Redirecting to login.");
            return "redirect:/login";
        }

        try {
            User currentUser = memberService.findUserByEmail(email)
                                                  .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

            model.addAttribute("name", currentUser.getName());
            model.addAttribute("userCoin", currentUser.getCoin());
            model.addAttribute("currentTheme", currentUser.getCurrentTheme()); // 使用正確的 Getter

            // 獲取用戶已購買的主題名稱列表，用於下拉選單
            List<String> userPurchasedThemeNames = userThemeService.getPurchasedThemeNamesByUserId(currentUser.getId());
            model.addAttribute("availableThemes", userPurchasedThemeNames);

            // 獲取所有可購買的主題，用於商城顯示
            List<Theme> allShopThemes = themeService.getAllThemes();
            model.addAttribute("allThemes", allShopThemes); // 將這個列表傳給前端

            logger.info("User {} accessed welcome page. Coin: {}, Theme: {}.",
                        email, currentUser.getCoin(), currentUser.getCurrentTheme());

        } catch (RuntimeException e) {
            logger.error("Error loading user data for {}: {}", email, e.getMessage());
            session.invalidate();
            if (e.getMessage().contains("User not found")) {
                return "redirect:/login?error=userNotFound";
            }
            model.addAttribute("error", "無法載入您的資料，請稍後再試。");
            return "error_page";
        }

        return "welcome";
    }

    // --- 新增：處理前端 AJAX 請求，保存用戶選擇的佈景主題 ---
    @PostMapping("/saveTheme")
    @ResponseBody
    public Map<String, String> saveTheme(@RequestBody Map<String, String> payload,
                                         HttpSession session) {

        String email = (String) session.getAttribute("email");
        if (email == null) {
            logger.warn("Unauthorized attempt to save theme: no active session.");
            return createErrorResponse("請先登入。");
        }

        String selectedThemeName = payload.get("theme");
        if (selectedThemeName == null || selectedThemeName.isEmpty()) {
            logger.warn("Save theme request from {} with missing theme name.", email);
            return createErrorResponse("主題名稱不能為空。");
        }

        try {
            User currentUser = memberService.findUserByEmail(email)
                                                  .orElseThrow(() -> new RuntimeException("User not found."));

            // 檢查用戶是否已擁有此主題 (包括預設主題)
            boolean hasPurchased = userThemeService.hasUserPurchasedTheme(currentUser.getId(), selectedThemeName);

            if (!hasPurchased) {
                logger.warn("User {} attempted to use unpurchased theme: {}", email, selectedThemeName);
                return createErrorResponse("您尚未擁有此主題，請先購買。");
            }

            // 更新用戶當前主題
            memberService.updateThemeByEmail(email, selectedThemeName);
            logger.info("User {} successfully updated theme to {}.", email, selectedThemeName);
            return createSuccessResponse("佈景主題已成功更新。");

        } catch (Exception e) {
            logger.error("Failed to save theme for user {}: {}", email, e.getMessage());
            return createErrorResponse("更新佈景主題失敗，請稍後再試。");
        }
    }

    // --- 新增：處理購買主題的 AJAX 請求 ---
    @PostMapping("/buyTheme")
    @ResponseBody
    public Map<String, Object> buyTheme(@RequestBody Map<String, String> payload,
                                        HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            logger.warn("Unauthorized attempt to buy theme: no active session.");
            return createErrorResponseMap("請先登入。");
        }

        String themeName = payload.get("theme");
        if (themeName == null || themeName.isEmpty()) {
            logger.warn("Buy theme request from {} with missing theme name.", email);
            return createErrorResponseMap("主題名稱不能為空。");
        }

        try {
            User currentUser = memberService.findUserByEmail(email)
                                                  .orElseThrow(() -> new RuntimeException("User not found."));

            Theme themeToBuy = themeService.findByThemeName(themeName)
                                            .orElseThrow(() -> new RuntimeException("主題不存在。"));

            if (userThemeService.hasUserPurchasedTheme(currentUser.getId(), themeName)) {
                logger.warn("User {} attempted to repurchase theme: {}", email, themeName);
                return createErrorResponseMap("您已擁有此主題。");
            }

            if (currentUser.getCoin() < themeToBuy.getPrice()) {
                logger.warn("User {} has insufficient coin to buy theme {}. Current coin: {}, Theme price: {}",
                            email, themeName, currentUser.getCoin(), themeToBuy.getPrice());
                return createErrorResponseMap("金幣不足，無法購買。");
            }

            // 扣除金幣並記錄購買
            currentUser.setCoin(currentUser.getCoin() - themeToBuy.getPrice());
            memberService.updateUserCoin(currentUser); // 更新用戶金幣

            userThemeService.addUserTheme(currentUser.getId(), themeToBuy.getId()); // 保存購買記錄

            logger.info("User {} successfully bought theme {}. New coin balance: {}",
                        email, themeName, currentUser.getCoin());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "購買成功！");
            response.put("newCoin", currentUser.getCoin()); // 返回新的金幣餘額給前端更新
            return response;

        } catch (Exception e) {
            logger.error("Failed to buy theme for user {}: {}", email, e.getMessage());
            return createErrorResponseMap("購買主題失敗，請稍後再試。");
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

    // 輔助方法：創建成功的 JSON 回應
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }

    // 輔助方法：創建錯誤的 JSON 回應 (String value)
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

    // 輔助方法：創建錯誤的 JSON 回應 (Object value for buyTheme)
    private Map<String, Object> createErrorResponseMap(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }
}