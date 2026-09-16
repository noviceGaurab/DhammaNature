package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.service.AuthService;
import io.virinchi.dhammanature.service.EmailService;
import io.virinchi.dhammanature.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionUserResolver sessionUserResolver;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                         HttpSession session, Model model) {
        return authService.authenticate(email, password)
                .map(user -> {
                    sessionUserResolver.login(session, user);
                    emailService.sendToUser(user, "You've logged in to Dhamma Nature",
                            "Hi " + user.getFullName() + ",\n\n"
                                    + "You just logged in to your account. If this wasn't you, "
                                    + "please reset your password immediately.\n\n"
                                    + "With metta,\nThe Dhamma Nature team");
                    if (user.getRole() == io.virinchi.dhammanature.model.enums.Role.ADMIN) {
                        return "redirect:/admin";
                    }
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Username or Password Incorrect!");
                    return "login";
                });
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam(name = "Email") String email,
                          @RequestParam(name = "Password") String password,
                          @RequestParam(name = "Gender_Identity", required = false) String genderIdentity,
                          @RequestParam(name = "acceptTerms", required = false) String acceptTerms,
                          Model model) {
        boolean agreed = "on".equals(acceptTerms);
        try {
            authService.signup(null, email, password, genderIdentity, agreed, agreed, agreed);
            return "redirect:/signup/success";
        } catch (Exception e) {
            model.addAttribute("signupfail", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/signup/success")
    public String signupSuccess() {
        return "post-signup";
    }

    // ===== Forgot / reset password =====

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        passwordResetService.requestPasswordReset(email);
        model.addAttribute("sent", true);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model, RedirectAttributes redirectAttributes) {
        if (password == null || password.length() < 8) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 8 characters long.");
            return "reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "The passwords do not match.");
            return "reset-password";
        }
        if (!passwordResetService.resetPassword(token, password)) {
            model.addAttribute("error", "This reset link is invalid or has expired. Please request a new one.");
            return "reset-password";
        }
        redirectAttributes.addFlashAttribute("resetSuccess", "Your password has been reset. Please log in with your new password.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        sessionUserResolver.logout(session);
        return "redirect:/login";
    }
}
