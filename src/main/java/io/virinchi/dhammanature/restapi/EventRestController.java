package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.PaymentMethod;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.service.BookingService;
import io.virinchi.dhammanature.service.EventService;
import io.virinchi.dhammanature.service.MeditationCenterService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final MeditationCenterService meditationCenterService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(eventService.upcoming().stream().map(ApiViews::event).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") Integer id) {
        try {
            Event event = eventService.get(id);
            Map<String, Object> body = ApiViews.event(event);
            body.put("bookingsTaken", bookingService.forEvent(id).size());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EventCreateRequest request) {
        try {
            var center = meditationCenterService.get(request.centerId());
            Event event = Event.builder()
                    .title(request.title())
                    .description(request.description())
                    .eventDate(request.eventDate())
                    .venue(request.venue())
                    .mode(request.mode() == null ? SessionMode.PHYSICAL : SessionMode.valueOf(request.mode()))
                    .capacity(request.capacity())
                    .price(request.price() == null ? BigDecimal.ZERO : request.price())
                    .build();
            Event saved = eventService.publish(center, event);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.event(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<?> book(@PathVariable Integer id,
                                  @RequestBody BookingRequest request,
                                  HttpSession session) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Please log in first."));
        }
        try {
            SessionMode mode = request.mode() == null ? SessionMode.PHYSICAL : SessionMode.valueOf(request.mode());
            PaymentMethod payment = request.paymentMethod() == null
                    ? null
                    : PaymentMethod.valueOf(request.paymentMethod());
            int attendees = request.attendees() == null || request.attendees() < 1 ? 1 : request.attendees();
            var booking = bookingService.book(userOpt.get(), id, mode, attendees,
                    payment, request.pointsToUse() == null ? 0 : request.pointsToUse(),
                    null, request.credential(), request.etiquetteAgreed() != null && request.etiquetteAgreed());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.booking(booking));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> myBookings(HttpSession session) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Please log in first."));
        }
        User user = userOpt.get();
        return ResponseEntity.ok(bookingService.forUser(user.getId()).stream().map(ApiViews::booking).toList());
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id, HttpSession session) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Please log in first."));
        }
        try {
            bookingService.cancel(userOpt.get(), id);
            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record EventCreateRequest(String title, String description, LocalDateTime eventDate,
                                     String venue, String mode, Integer capacity,
                                     BigDecimal price, Integer centerId) {
    }

    public record BookingRequest(String mode, Integer attendees, String paymentMethod,
                                 Integer pointsToUse, String credential, Boolean etiquetteAgreed) {
    }
}