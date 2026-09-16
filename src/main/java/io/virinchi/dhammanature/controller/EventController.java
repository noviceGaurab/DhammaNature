package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.service.BookingService;
import io.virinchi.dhammanature.service.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

/** FR-04: Event Management + FR-03/NFR-02: booking with hybrid session mode. */
@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/events")
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        var events = eventService.upcoming();
        int pageSize = 6;
        int totalPages = Math.max(1, (int) Math.ceil(events.size() / (double) pageSize));
        int current = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("events", events.stream()
                .skip((current - 1) * (long) pageSize).limit(pageSize).toList());
        model.addAttribute("eventCount", events.size());
        model.addAttribute("onlineCount", events.stream().filter(e -> e.getMode() != null && e.getMode().name().contains("ONLINE")).count());
        model.addAttribute("physicalCount", events.stream().filter(e -> e.getMode() != null && e.getMode().name().contains("PHYSICAL")).count());
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageBase", "/events");
        return "events";
    }

    @GetMapping("/events/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var event = eventService.get(id);
        model.addAttribute("event", event);
        model.addAttribute("relatedEvents", eventService.upcoming().stream()
                .filter(e -> !e.getId().equals(id))
                .limit(3)
                .toList());
        return "event-detail";
    }

    @PostMapping("/events/{id}/book")
    public String book(@PathVariable Integer id,
                        @RequestParam(defaultValue = "PHYSICAL") SessionMode mode,
                        @RequestParam(defaultValue = "1") int attendees,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    var booking = bookingService.book(user, id, mode, attendees);
                    redirectAttributes.addFlashAttribute("bookingId", booking.getId());
                    return "redirect:/events/confirmed";
                })
                .orElse("redirect:/login");
    }

    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable Integer id, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        bookingService.cancel(user, id);
                        redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/profile";
                })
                .orElse("redirect:/login");
    }

    @GetMapping("/events/confirmed")
    public String confirmed(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer bookingId,
                            HttpSession session, Model model) {
        Integer resolvedId = bookingId != null ? bookingId : (Integer) model.getAttribute("bookingId");
        if (resolvedId == null) {
            return "redirect:/events";
        }
        try {
            var booking = bookingService.get(resolvedId);
            if (!sessionUserResolver.resolve(session)
                    .map(u -> u.getId().equals(booking.getUser().getId()))
                    .orElse(false)) {
                return "redirect:/events";
            }
            model.addAttribute("booking", booking);
            model.addAttribute("pointsEarned", 5);
        } catch (Exception e) {
            return "redirect:/events";
        }
        return "booking-confirm";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleMissing(NoSuchElementException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage() + " - please choose an event from the list.");
        return "redirect:/events";
    }
}
