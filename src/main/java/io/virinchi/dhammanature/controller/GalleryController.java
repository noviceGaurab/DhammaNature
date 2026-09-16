package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping("/gallery")
    public String gallery(@RequestParam(defaultValue = "1") int page, Model model) {
        var images = galleryService.all();
        int pageSize = 12;
        int totalPages = Math.max(1, (int) Math.ceil(images.size() / (double) pageSize));
        int current = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("images", images.stream()
                .skip((current - 1) * (long) pageSize).limit(pageSize).toList());
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageBase", "/gallery");
        return "gallery";
    }
}
