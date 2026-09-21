package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.service.CharityCampaignService;
import io.virinchi.dhammanature.service.EventService;
import io.virinchi.dhammanature.service.MeditationCenterService;
import io.virinchi.dhammanature.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/centers")
public class MeditationCenterRestController {

    private final MeditationCenterService meditationCenterService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final CharityCampaignService charityCampaignService;

    @GetMapping
    public ResponseEntity<?> directory(@RequestParam(required = false) String q) {
        var centers = (q == null || q.isBlank())
                ? meditationCenterService.directory()
                : meditationCenterService.search(q);
        return ResponseEntity.ok(centers.stream().map(ApiViews::center).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> profile(@PathVariable Integer id) {
        var center = meditationCenterService.get(id);
        Map<String, Object> body = ApiViews.center(center);
        body.put("eventCount", eventService.forCenter(id).size());
        body.put("opportunityCount", volunteerService.forCenter(id).size());
        body.put("campaignCount", charityCampaignService.forCenter(id).size());
        body.put("programCount", center.getPrograms().size());
        body.put("events", eventService.forCenter(id).stream().map(ApiViews::event).toList());
        body.put("volunteerOpportunities",
                volunteerService.forCenter(id).stream().map(ApiViews::volunteerOpportunity).toList());
        body.put("charityCampaigns",
                charityCampaignService.forCenter(id).stream().map(ApiViews::campaign).toList());
        return ResponseEntity.ok(body);
    }
}
