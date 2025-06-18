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
import com.example.demo.service.MemberService;
import com.example.demo.service.ResetPasswordService;
import com.example.demo.service.VerificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private ResetPasswordService resetPasswordService;

    // 首頁處理 (已登入導向歡迎頁，未登入導向 /index)
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email != null) {
            String name = memberService.getNameByUsername(email);
            model.addAttribute("name", name);
            logger.info("User {} is logged in. Redirecting to /welcome.", email);
            return "redirect:/welcome"; // 已登入導向 /welcome
        }
        logger.info("User not logged in. Redirecting to /index.");
        return "redirect:/index"; // 未登入導向 /index (新的未登入首頁)
    }

    // 顯示未登入首頁 (新增)
    @GetMapping("/index")
    public String index() {
        return "index"; // 對應到你的 src/main/resources/templates/index.html
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

    // 顯示忘記密碼頁面
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
            // 為了避免洩漏用戶是否存在，即使失敗也給出類似提示
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