package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByMeditationCenter_IdOrderByEventDateAsc(Integer centerId);
    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime after);
    Page<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime after, Pageable pageable);
    long countByEventDateAfter(LocalDateTime after);
    List<Event> findAllByOrderByEventDateAsc();
}
