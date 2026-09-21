package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.Role;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Session-based authorization for the REST API. The web app authenticates via the
 * HTTP session, so write endpoints must resolve the caller from that session and
 * reject anonymous (401) or under-privileged (403) requests.
 */
@Component
@RequiredArgsConstructor
public class ApiAuth {

    private final SessionUserResolver sessionUserResolver;

    /** Returns the logged-in user or throws 401. */
    public User requireUser(HttpSession session) {
        return sessionUserResolver.resolve(session)
                .orElseThrow(() -> ApiException.unauthorized("Please log in first."));
    }

    /** Returns the logged-in user or null (for endpoints that allow guests). */
    public User currentUser(HttpSession session) {
        return sessionUserResolver.resolve(session).orElse(null);
    }

    /** Returns the logged-in administrator or throws 401/403. */
    public User requireAdmin(HttpSession session) {
        User user = requireUser(session);
        if (user.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("Only an administrator may perform this action.");
        }
        return user;
    }
}
