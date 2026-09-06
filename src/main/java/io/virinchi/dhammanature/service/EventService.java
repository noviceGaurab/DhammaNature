package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.MeditationCenter;
import io.virinchi.dhammanature.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/** FR-04: Event Management. */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> upcoming() {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());
    }

    public List<Event> all() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    public Event get(Integer id) {
        return eventRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Event not found"));
    }

    public Event publish(MeditationCenter center, Event event) {
        event.setMeditationCenter(center);
        return eventRepository.save(event);
    }

    public List<Event> forCenter(Integer centerId) {
        return eventRepository.findByMeditationCenter_IdOrderByEventDateAsc(centerId);
    }
}
