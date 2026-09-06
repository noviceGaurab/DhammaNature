package io.virinchi.dhammanature.config;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionUserResolver {

    private final UserRepository userRepository;

    public Optional<User> resolve(HttpSession session) {
        Object id = session.getAttribute(GlobalModelAttributes.SESSION_USER_ID);
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById((Integer) id);
    }

    public User require(HttpSession session) {
        return resolve(session).orElseThrow(() -> new IllegalStateException("Please log in first."));
    }

    public void login(HttpSession session, User user) {
        session.setAttribute(GlobalModelAttributes.SESSION_USER_ID, user.getId());
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
