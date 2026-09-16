package io.virinchi.dhammanature.config;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.service.UserActivityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes the logged-in user (or null) available as "currentUser" in every
 * Thymeleaf template, so the shared header fragment can switch between
 * "Login" and "Profile / Logout" without every controller wiring it manually.
 * Also records a light "last seen" heartbeat used by the discuss AFK nudges.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    public static final String SESSION_USER_ID = "userId";

    private final UserRepository userRepository;
    private final UserActivityService userActivityService;

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (id == null) {
            return null;
        }
        Integer userId = (Integer) id;
        userActivityService.recordActivity(userId);
        return userRepository.findById(userId).orElse(null);
    }
}