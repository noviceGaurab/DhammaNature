package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionUserResolver sessionUserResolver;

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
                          @RequestParam(name = "readGuidelines", required = false) String readGuidelines,
                          @RequestParam(name = "understoodGuidelines", required = false) String understoodGuidelines,
                          @RequestParam(name = "acceptTerms", required = false) String acceptTerms,
                          Model model) {
        try {
            User user = authService.signup(null, email, password, genderIdentity,
                    "on".equals(readGuidelines), "on".equals(understoodGuidelines), "on".equals(acceptTerms));
            model.addAttribute("signupsuccessful", "Signed Up Successfully! Please Log In");
            return "login";
        } catch (Exception e) {
            model.addAttribute("signupfail", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        sessionUserResolver.logout(session);
        return "redirect:/login";
    }
}
