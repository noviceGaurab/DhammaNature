package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventRestController {

    private final EventRepository eventRepository;

    @GetMapping
    public List<Event> getAll() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") Integer id) {
        return eventRepository.findById(id)
                .map(event -> ResponseEntity.ok((Object) event))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Event not found"));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Event event) {
        Event saved = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}