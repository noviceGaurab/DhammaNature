package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByMeditationCenter_IdOrderByEventDateAsc(Integer centerId);
    List<Event> findByEventDateAfterOrderByEventDateAsc(java.time.LocalDateTime after);
    List<Event> findAllByOrderByEventDateAsc();
}
