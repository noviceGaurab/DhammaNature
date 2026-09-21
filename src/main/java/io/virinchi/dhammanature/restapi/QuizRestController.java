package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.Quiz;
import io.virinchi.dhammanature.service.QuizService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
public class QuizRestController {

    private final QuizService quizService;
    private final ApiAuth apiAuth;

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(quizService.all().stream().map(ApiViews::quiz).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Integer id) {
        Quiz quiz = quizService.get(id);
        Map<String, Object> body = ApiViews.quiz(quiz);
        body.put("questions", quizService.questionsFor(id).stream().map(ApiViews::quizQuestion).toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> leaderboard() {
        return ResponseEntity.ok(quizService.leaderboard().stream().map(ApiViews::quizAttempt).toList());
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Integer id,
                                    @Valid @RequestBody QuizSubmitRequest request,
                                    HttpSession session) {
        var user = apiAuth.requireUser(session);
        var attempt = quizService.submit(user, id, request.answers());
        return ResponseEntity.ok(Map.of(
                "message", "Quiz attempt recorded.",
                "score", attempt.getScore(),
                "totalQuestions", attempt.getTotalQuestions(),
                "pointsRemaining", user.getRewardPoints()));
    }

    public record QuizSubmitRequest(
            @NotNull(message = "Answers are required.")
            Map<Integer, String> answers) {
    }
}
