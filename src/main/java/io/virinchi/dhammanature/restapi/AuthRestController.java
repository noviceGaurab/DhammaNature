package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.dto.SignupRequest;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthService authService;
    private final SessionUserResolver sessionUserResolver;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request, HttpSession session) {
        boolean agreed = request.acceptTerms() != null && request.acceptTerms();
        User saved = authService.signup(request.fullName(), request.email(),
                request.password(), request.genderIdentity(), agreed, agreed, agreed);
        sessionUserResolver.login(session, saved);
        Map<String, Object> body = ApiViews.user(saved);
        body.put("message", "Sign up successful.");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        User user = authService.authenticate(request.email(), request.password())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password."));
        sessionUserResolver.login(session, user);
        Map<String, Object> body = ApiViews.user(user);
        body.put("message", "Logged in successfully.");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(HttpSession session) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("loggedIn", false);
            body.put("message", "Not logged in.");
            return ResponseEntity.ok(body);
        }
        Map<String, Object> body = ApiViews.user(userOpt.get());
        body.put("loggedIn", true);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        sessionUserResolver.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email,

            @NotBlank(message = "Password is required.")
            String password) {
    }
}
