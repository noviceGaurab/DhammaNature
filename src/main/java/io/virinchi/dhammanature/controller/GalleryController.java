package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        int pageSize = 12;
        var paged = galleryService.pagedAll(PageRequest.of(Math.max(0, page - 1), pageSize));
        int current = coercePage(page, paged.getTotalPages());
        if (current != Math.max(1, page)) {
            paged = galleryService.pagedAll(PageRequest.of(current - 1, pageSize));
        }
        model.addAttribute("images", paged.getContent());
        model.addAttribute("page", current);
        model.addAttribute("totalPages", paged.getTotalPages());
        model.addAttribute("pageBase", "/gallery");
        return "gallery";
    }

    private int coercePage(int requested, int totalPages) {
        return Math.max(1, Math.min(requested, Math.max(1, totalPages)));
    }
}
