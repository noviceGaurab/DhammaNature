package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.CharityCampaignService;
import io.virinchi.dhammanature.service.EventService;
import io.virinchi.dhammanature.service.MeditationCenterService;
import io.virinchi.dhammanature.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

/** FR-03: Meditation Center Directory + NFR-03: Organization Profile. */
@Controller
@RequiredArgsConstructor
public class MeditationCenterController {

    private final MeditationCenterService meditationCenterService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final CharityCampaignService charityCampaignService;

    @GetMapping("/centers")
    public String directory(@RequestParam(required = false) String q, Model model) {
        var centers = (q == null || q.isBlank())
                ? meditationCenterService.directory()
                : meditationCenterService.search(q);
        model.addAttribute("centers", centers);
        model.addAttribute("q", q);
        model.addAttribute("centerCount", centers.size());
        model.addAttribute("hybridCount", centers.stream()
                .filter(c -> c.isSupportsOnlineSessions() && c.isSupportsPhysicalSessions()).count());
        model.addAttribute("onlineOnlyCount", centers.stream()
                .filter(c -> c.isSupportsOnlineSessions() && !c.isSupportsPhysicalSessions()).count());
        return "centers";
    }

    @GetMapping("/centers/{id}")
    public String profile(@PathVariable Integer id, Model model) {
        var center = meditationCenterService.get(id);
        var events = eventService.forCenter(id);
        var opportunities = volunteerService.forCenter(id);
        model.addAttribute("center", center);
        model.addAttribute("events", events);
        model.addAttribute("volunteerOpportunities", opportunities);
        model.addAttribute("charityCampaigns", charityCampaignService.forCenter(id));
        model.addAttribute("programCount", center.getPrograms().size());
        model.addAttribute("eventCount", events.size());
        model.addAttribute("opportunityCount", opportunities.size());
        return "center-detail";
    }
}
