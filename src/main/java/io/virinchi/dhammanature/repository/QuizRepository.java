package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {

}
