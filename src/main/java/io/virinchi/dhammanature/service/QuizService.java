package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Quiz;
import io.virinchi.dhammanature.model.QuizAttempt;
import io.virinchi.dhammanature.model.QuizQuestion;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.QuizAttemptRepository;
import io.virinchi.dhammanature.repository.QuizQuestionRepository;
import io.virinchi.dhammanature.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** FR-08: Quiz Module - encourages Dhamma learning and feeds the reward system (FR-09). */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final RewardService rewardService;

    public List<Quiz> all() {
        return quizRepository.findAllWithQuestions();
    }

    public Quiz get(Integer id) {
        return quizRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    }

    public List<QuizQuestion> questionsFor(Integer quizId) {
        return quizQuestionRepository.findByQuiz_Id(quizId);
    }

    /** answers: questionId -> chosen option ("A"/"B"/"C"/"D"). */
    @Transactional
    public QuizAttempt submit(User user, Integer quizId, Map<Integer, String> answers) {
        Quiz quiz = get(quizId);
        List<QuizQuestion> questions = questionsFor(quizId);
        int score = 0;
        for (QuizQuestion q : questions) {
            String chosen = answers.get(q.getId());
            if (chosen != null && chosen.equalsIgnoreCase(q.getCorrectOption())) {
                score++;
            }
        }
        QuizAttempt attempt = quizAttemptRepository.save(QuizAttempt.builder()
                .user(user).quiz(quiz).score(score).totalQuestions(questions.size()).build());

        if (!questions.isEmpty() && score == questions.size()) {
            rewardService.awardPoints(user, quiz.getRewardPoints(), "Perfect score on quiz \"" + quiz.getTitle() + "\"");
        } else if (score > 0) {
            rewardService.awardPoints(user, Math.max(1, quiz.getRewardPoints() / 2), "Completed quiz \"" + quiz.getTitle() + "\"");
        }
        return attempt;
    }

    public List<QuizAttempt> historyFor(Integer userId) {
        return quizAttemptRepository.findByUser_IdOrderByAttemptedAtDesc(userId);
    }

    /** Top 5 quiz performances across the whole community, for the landing page. */
    public List<QuizAttempt> leaderboard() {
        return quizAttemptRepository.findTop5ByOrderByScoreDescAttemptedAtDesc();
    }

    /** Creates a quiz together with its questions. Admin-published quizzes appear on the live quiz page immediately. */
    @Transactional
    public Quiz createQuiz(String title, String description, int rewardPoints, List<QuizQuestion> questions) {
        Quiz quiz = Quiz.builder()
                .title(title)
                .description(description)
                .rewardPoints(rewardPoints)
                .build();
        questions.forEach(q -> {
            q.setId(null);
            q.setQuiz(quiz);
            quiz.getQuestions().add(q);
        });
        return quizRepository.save(quiz);
    }

    /** Removes the quiz, its questions and any attempts (cascade orphanRemoval). */
    @Transactional
    public void delete(Integer id) {
        if (!quizRepository.existsById(id)) {
            throw new NoSuchElementException("Quiz not found");
        }
        quizRepository.deleteById(id);
    }
}
