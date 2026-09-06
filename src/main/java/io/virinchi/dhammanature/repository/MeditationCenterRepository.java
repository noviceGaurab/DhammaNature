package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.MeditationCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeditationCenterRepository extends JpaRepository<MeditationCenter, Integer> {
    List<MeditationCenter> findByVerifiedTrue();
    List<MeditationCenter> findByNameContainingIgnoreCase(String name);
}
