package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.service.CharityCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** NFR-05: Charity Campaign Module. */
@Controller
@RequiredArgsConstructor
public class CharityController {

    private final CharityCampaignService charityCampaignService;

    @GetMapping("/charity")
    public String list(Model model) {
        model.addAttribute("campaigns", charityCampaignService.active());
        return "charity";
    }

    @GetMapping("/charity/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("campaign", charityCampaignService.get(id));
        return "redirect:/donate?campaignId=" + id;
    }
}
