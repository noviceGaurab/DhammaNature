package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.QuizAttempt;
import io.virinchi.dhammanature.service.QuizService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** FR-08: Quiz Module - encourages Dhamma learning and feeds the reward system. */
@Controller
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/quiz")
    public String list(Model model) {
        model.addAttribute("quizzes", quizService.all());
        return "quiz";
    }

    @GetMapping("/quiz/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("quiz", quizService.get(id));
        model.addAttribute("questions", quizService.questionsFor(id));
        return "quiz-detail";
    }

    @PostMapping("/quiz/{id}/submit")
    public String submit(@PathVariable Integer id, @RequestParam Map<String, String> allParams,
                          HttpSession session, Model model) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    Map<Integer, String> answers = new HashMap<>();
                    allParams.forEach((k, v) -> {
                        if (k.startsWith("q_")) {
                            answers.put(Integer.valueOf(k.substring(2)), v);
                        }
                    });
                    QuizAttempt attempt = quizService.submit(user, id, answers);
                    model.addAttribute("attempt", attempt);
                    model.addAttribute("quiz", quizService.get(id));
                    return "quiz-result";
                })
                .orElse("redirect:/login");
    }
}
