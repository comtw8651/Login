package com.example.demo.controller;

import com.example.demo.entity.Theme;
import com.example.demo.entity.User; // 請替換成你的實際套件路徑
import com.example.demo.repository.UserRepository; // 請替換成你的實際套件路徑
import com.example.demo.service.MemberService;
import com.example.demo.service.ThemeService;
import com.example.demo.service.UserThemeService;

import jakarta.servlet.http.HttpSession; // 引入 HttpSession

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class SettingController {

	private static final Logger logger = LoggerFactory.getLogger(SettingController.class);
	
	@Autowired
    private UserRepository userRepository; // 注入 UserRepository
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private ThemeService themeService;
    
    @Autowired
    private UserThemeService userThemeService;

    /**
     * 顯示個人資料頁面。
     * 從 Session 中獲取當前登入的使用者 Email。
     */
    @GetMapping("/setting") // 將路徑改為 /setting
    public String showSettings(Model model, HttpSession session) {
        String loggedInUserEmail = (String) session.getAttribute("loggedInUserEmail"); // 從 Session 獲取使用者 Email

        if (loggedInUserEmail == null) {
            // 如果 Session 中沒有使用者 Email，表示未登入，重定向到登入頁面
            return "redirect:/login"; // 假設你有一個 /login 頁面
        }
        
        try {
            User currentUser = memberService.findUserByEmail(loggedInUserEmail)
                                                  .orElseThrow(() -> new RuntimeException("User not found for email: " + loggedInUserEmail));

            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("email", currentUser.getEmail());
            model.addAttribute("password", currentUser.getPassword());
            model.addAttribute("userCoin", currentUser.getCoin());
            model.addAttribute("currentTheme", currentUser.getCurrentTheme());

            // 獲取用戶已購買的主題名稱列表，用於下拉選單
            List<String> userPurchasedThemeNames = userThemeService.getPurchasedThemeNamesByUserId(currentUser.getId());
            model.addAttribute("availableThemes", userPurchasedThemeNames);

            // 獲取所有可購買的主題，用於商城顯示
            List<Theme> allShopThemes = themeService.getAllThemes();
            model.addAttribute("allThemes", allShopThemes);

            logger.info("User {} accessed welcome page. Coin: {}, Theme: {}.",
            		loggedInUserEmail, currentUser.getCoin(), currentUser.getCurrentTheme());

        } catch (RuntimeException e) {
            logger.error("Error loading user data for {}: {}", loggedInUserEmail, e.getMessage());
            session.invalidate(); // 如果用戶數據加載失敗，安全起見登出
            if (e.getMessage().contains("User not found")) {
                return "redirect:/login?error=userNotFound";
            }
            model.addAttribute("error", "無法載入您的資料，請稍後再試。");
            return "error_page"; // 或者導向到一個通用錯誤頁面
        }


        Optional<User> userOptional = userRepository.findByEmail(loggedInUserEmail);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user); // 將使用者物件添加到模型中
        } else {
            // 處理使用者不存在的情況 (例如資料庫中找不到該 Email)
            System.err.println("Error: User with email " + loggedInUserEmail + " not found in DB!");
            session.invalidate(); // 清除 Session，強制重新登入
            model.addAttribute("errorMessage", "無法找到使用者資料，請重新登入。");
            return "redirect:/login";
        }
        return "setting"; // 返回 settings.html 模板名稱
    }

    /**
     * 處理更新使用者名稱的請求。
     */
    @PostMapping("/setting/updateName") // 將路徑改為 /settings/updateName
    public String updateUserName(@RequestParam("name") String newName,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        String loggedInUserEmail = (String) session.getAttribute("loggedInUserEmail");

        if (loggedInUserEmail == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請先登入。");
            return "redirect:/login";
        }

        Optional<User> userOptional = userRepository.findByEmail(loggedInUserEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUsername(newName);
            userRepository.save(user); // 儲存更新後的使用者資料
            redirectAttributes.addFlashAttribute("successMessage", "姓名已成功更新！");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：找不到使用者。");
        }
        return "redirect:/setting"; // 重定向回個人資料頁面
    }

    /**
     * 處理更新使用者Email的請求。
     */
    @PostMapping("/setting/updateEmail") // 將路徑改為 /settings/updateEmail
    public String updateUserEmail(@RequestParam("email") String newEmail,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        String loggedInUserEmail = (String) session.getAttribute("loggedInUserEmail");

        if (loggedInUserEmail == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請先登入。");
            return "redirect:/login";
        }

        // 驗證新 email 是否已被使用 (除了當前使用者自己)
        Optional<User> existingUserWithNewEmail = userRepository.findByEmail(newEmail);
        if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getEmail().equals(loggedInUserEmail)) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：此電子郵件已被其他帳戶使用。");
            return "redirect:/setting";
        }

        Optional<User> userOptional = userRepository.findByEmail(loggedInUserEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmail(newEmail);
            userRepository.save(user); // 儲存更新後的使用者資料
            // 更新 Session 中的 Email，因為使用者的 Email 變了
            session.setAttribute("loggedInUserEmail", newEmail);
            redirectAttributes.addFlashAttribute("successMessage", "郵箱已成功更新！");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：找不到使用者。");
        }
        return "redirect:/setting";
    }
}