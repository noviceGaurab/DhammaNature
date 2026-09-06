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

/** FR-04: Event Management + FR-03/NFR-02: booking with hybrid session mode. */
@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/events")
    public String list(Model model) {
        model.addAttribute("events", eventService.upcoming());
        return "events";
    }

    @GetMapping("/events/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("event", eventService.get(id));
        return "event-detail";
    }

    @PostMapping("/events/{id}/book")
    public String book(@PathVariable Integer id,
                        @RequestParam(defaultValue = "PHYSICAL") SessionMode mode,
                        @RequestParam(defaultValue = "1") int attendees,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        bookingService.book(user, id, mode, attendees);
                        redirectAttributes.addFlashAttribute("success", "Booking confirmed! You earned 5 reward points.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/events/" + id;
                })
                .orElse("redirect:/login");
    }
}
