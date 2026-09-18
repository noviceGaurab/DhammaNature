package io.virinchi.dhammanature.config;

import io.virinchi.dhammanature.model.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Gates every request under /admin/** to signed-in users with the ADMIN role. */
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final SessionUserResolver sessionUserResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            response.sendRedirect("/login");
            return false;
        }
        if (userOpt.get().getRole() != Role.ADMIN) {
            response.sendRedirect("/");
            return false;
        }
        return true;
    }
}