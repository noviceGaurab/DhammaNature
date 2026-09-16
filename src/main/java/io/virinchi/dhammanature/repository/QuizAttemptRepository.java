package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.QuizAttempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {

    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.quiz WHERE a.user.id = :userId ORDER BY a.attemptedAt DESC")
    List<QuizAttempt> findByUser_IdOrderByAttemptedAtDesc(@Param("userId") Integer userId);

    /** Top scorers for the quiz landing page leaderboard. */
    @EntityGraph(attributePaths = {"user", "quiz"})
    List<QuizAttempt> findTop5ByOrderByScoreDescAttemptedAtDesc();

    long countByQuiz_Id(Integer quizId);
}
