package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer> {
    List<QuizQuestion> findByQuiz_Id(Integer quizId);
}
