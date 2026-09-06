package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
    List<QuizAttempt> findByUser_IdOrderByAttemptedAtDesc(Integer userId);
}
