package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("images", galleryService.all());
        return "gallery";
    }
}
