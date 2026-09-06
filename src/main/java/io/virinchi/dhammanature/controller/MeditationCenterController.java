package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.EventService;
import io.virinchi.dhammanature.service.MeditationCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** FR-03: Meditation Center Directory + NFR-03: Organization Profile. */
@Controller
@RequiredArgsConstructor
public class MeditationCenterController {

    private final MeditationCenterService meditationCenterService;
    private final EventService eventService;

    @GetMapping("/centers")
    public String directory(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("centers", (q == null || q.isBlank())
                ? meditationCenterService.directory()
                : meditationCenterService.search(q));
        model.addAttribute("q", q);
        return "centers";
    }

    @GetMapping("/centers/{id}")
    public String profile(@PathVariable Integer id, Model model) {
        model.addAttribute("center", meditationCenterService.get(id));
        model.addAttribute("events", eventService.forCenter(id));
        return "center-detail";
    }
}
