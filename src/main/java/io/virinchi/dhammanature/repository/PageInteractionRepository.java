package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.PageInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageInteractionRepository extends JpaRepository<PageInteraction, Integer> {
    List<PageInteraction> findAllByOrderByInteractionTimeDesc();
}
