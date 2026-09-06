package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    List<Program> findByMeditationCenter_Id(Integer centerId);
}
