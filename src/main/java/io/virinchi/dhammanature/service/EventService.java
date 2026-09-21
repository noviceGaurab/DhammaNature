package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.MeditationCenter;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<Event> pagedUpcoming(Pageable pageable) {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now(), pageable);
    }

    public long countUpcoming() {
        return eventRepository.countByEventDateAfter(LocalDateTime.now());
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

    /** Removes the event and its participant bookings (orphanRemoval on Event.bookings). */
    @Transactional
    public void delete(Integer id) {
        if (!eventRepository.existsById(id)) {
            throw new NoSuchElementException("Event not found");
        }
        eventRepository.deleteById(id);
    }

    /** Admin edit of event details. Existing bookings are preserved. */
    @Transactional
    public Event update(Integer id, String title, String description, LocalDateTime eventDate,
                        String venue, SessionMode mode, Integer capacity, MeditationCenter center) {
        Event event = get(id);
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setVenue(venue);
        event.setMode(mode);
        event.setCapacity(capacity);
        event.setMeditationCenter(center);
        return eventRepository.save(event);
    }
}
