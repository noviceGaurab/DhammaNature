package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Handles the newsletter subscribe forms on the home and marketplace pages. */
@Controller
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/newsletter/subscribe")
    public String subscribe(@RequestParam("email") String email,
                            @RequestParam(required = false) String returnTo,
                            RedirectAttributes redirectAttributes) {
        String redirect = safeRedirect(returnTo);
        NewsletterService.SubscriptionResult result = newsletterService.subscribe(email);
        if (result.success()) {
            redirectAttributes.addFlashAttribute("newsletterOk", result.message());
        } else {
            redirectAttributes.addFlashAttribute("newsletterError", result.message());
        }
        return "redirect:" + redirect;
    }

    private String safeRedirect(String returnTo) {
        if (returnTo != null && returnTo.startsWith("/") && !returnTo.startsWith("//")
                && !returnTo.equals("/newsletter/subscribe")) {
            return returnTo;
        }
        return "/";
    }
}