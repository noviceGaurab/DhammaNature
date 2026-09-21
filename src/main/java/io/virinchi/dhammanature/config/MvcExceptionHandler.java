package io.virinchi.dhammanature.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

/**
 * Central MVC counterpart to the REST {@code ApiExceptionHandler}. A "not found"
 * while rendering a page redirects the visitor back to that section's list view
 * with an error flash, replacing the per-controller handlers that used to exist
 * in EventController, MarketplaceController and MeditationCenterController.
 */
@ControllerAdvice(basePackages = "io.virinchi.dhammanature.controller")
public class MvcExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public String handleMissing(NoSuchElementException ex, HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + sectionRoot(request.getRequestURI());
    }

    private String sectionRoot(String uri) {
        if (uri == null || uri.isBlank() || "/".equals(uri)) {
            return "/";
        }
        String[] parts = uri.split("/");
        for (String part : parts) {
            if (!part.isBlank()) {
                return "/" + part;
            }
        }
        return "/";
    }
}
