package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.enums.PaymentMethod;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.service.BookingService;
import io.virinchi.dhammanature.service.EventService;
import io.virinchi.dhammanature.service.MeditationCenterService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final MeditationCenterService meditationCenterService;
    private final ApiAuth apiAuth;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(eventService.upcoming().stream().map(ApiViews::event).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") Integer id) {
        Event event = eventService.get(id);
        var body = ApiViews.event(event);
        body.put("bookingsTaken", bookingService.forEvent(id).size());
        return ResponseEntity.ok(body);
    }

    /** Admin only: publishes a new event (401 when anonymous, 403 when not an administrator). */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EventCreateRequest request, HttpSession session) {
        apiAuth.requireAdmin(session);
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
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<?> book(@PathVariable Integer id,
                                  @Valid @RequestBody BookingRequest request,
                                  HttpSession session) {
        var user = apiAuth.requireUser(session);
        SessionMode mode = request.mode() == null ? SessionMode.PHYSICAL : SessionMode.valueOf(request.mode());
        PaymentMethod payment = request.paymentMethod() == null
                ? null
                : PaymentMethod.valueOf(request.paymentMethod());
        int attendees = request.attendees() == null || request.attendees() < 1 ? 1 : request.attendees();
        var booking = bookingService.book(user, id, mode, attendees,
                payment, request.pointsToUse() == null ? 0 : request.pointsToUse(),
                null, request.credential(), request.etiquetteAgreed() != null && request.etiquetteAgreed());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.booking(booking));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> myBookings(HttpSession session) {
        var user = apiAuth.requireUser(session);
        return ResponseEntity.ok(bookingService.forUser(user.getId()).stream().map(ApiViews::booking).toList());
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id, HttpSession session) {
        var user = apiAuth.requireUser(session);
        bookingService.cancel(user, id);
        return ResponseEntity.ok(java.util.Map.of("message", "Booking cancelled successfully."));
    }

    public record EventCreateRequest(
            @NotBlank(message = "Event title is required.")
            @Size(max = 200, message = "Title must be 200 characters or fewer.")
            String title,

            @Size(max = 5000, message = "Description is too long.")
            String description,

            LocalDateTime eventDate,

            @Size(max = 200, message = "Venue is too long.")
            String venue,

            @Size(max = 50, message = "Mode is too long.")
            String mode,

            @Min(value = 0, message = "Capacity cannot be negative.")
            Integer capacity,

            @DecimalMin(value = "0.0", message = "Price cannot be negative.")
            BigDecimal price,

            @NotNull(message = "centerId is required.")
            Integer centerId) {
    }

    public record BookingRequest(
            @Size(max = 50, message = "Mode is too long.")
            String mode,

            @Min(value = 1, message = "At least one attendee is required.")
            Integer attendees,

            @Size(max = 50, message = "Payment method is too long.")
            String paymentMethod,

            @Min(value = 0, message = "Points to use cannot be negative.")
            Integer pointsToUse,

            @Size(max = 500, message = "Credential is too long.")
            String credential,

            Boolean etiquetteAgreed) {
    }
}
