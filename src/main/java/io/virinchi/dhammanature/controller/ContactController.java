package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Public contact form: renders /contact and persists submitted messages. */
@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping("/contact")
    public String form() {
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@RequestParam String name,
                         @RequestParam String email,
                         @RequestParam String subject,
                         @RequestParam String message,
                         Model model) {
        model.addAttribute("contactName", name);
        model.addAttribute("contactEmail", email);
        model.addAttribute("contactSubject", subject);
        model.addAttribute("contactMessage", message);
        try {
            contactService.submit(name, email, subject, message);
            model.addAttribute("success", "Thank you, " + name.trim() + "! Your message has been received - we will reply within 24 hours.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "contact";
    }
}