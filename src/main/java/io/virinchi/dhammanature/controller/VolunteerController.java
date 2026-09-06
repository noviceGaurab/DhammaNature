package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.service.VolunteerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** NFR-06: Volunteer Registration - food distribution, cleaning, tree plantation, religious events. */
@Controller
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/volunteer")
    public String list(HttpSession session, Model model) {
        model.addAttribute("opportunities", volunteerService.upcoming());
        sessionUserResolver.resolve(session).ifPresent(u ->
                model.addAttribute("myRegistrations", volunteerService.forUser(u.getId())));
        return "volunteer";
    }

    @PostMapping("/volunteer/{id}/register")
    public String register(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        volunteerService.register(user, id);
                        redirectAttributes.addFlashAttribute("success", "You're registered! You earned 15 reward points.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/volunteer";
                })
                .orElse("redirect:/login");
    }
}
