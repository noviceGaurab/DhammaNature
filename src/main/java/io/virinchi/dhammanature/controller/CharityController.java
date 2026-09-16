package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.model.CharityCampaign;
import io.virinchi.dhammanature.service.CharityCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** NFR-05: Charity Campaign Module. */
@Controller
@RequiredArgsConstructor
public class CharityController {

    private final CharityCampaignService charityCampaignService;

    @GetMapping("/charity")
    public String list(Model model) {
        List<CharityCampaign> campaigns = charityCampaignService.active();
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("campaignCount", campaigns.size());
        model.addAttribute("raisedTotal", sumRaised(campaigns));
        model.addAttribute("goalTotal", sumGoal(campaigns));
        return "charity";
    }

    @GetMapping("/charity/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("campaign", charityCampaignService.get(id));
        return "redirect:/donate?campaignId=" + id;
    }

    private BigDecimal sumRaised(List<CharityCampaign> campaigns) {
        return campaigns.stream()
                .map(CharityCampaign::getRaisedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumGoal(List<CharityCampaign> campaigns) {
        return campaigns.stream()
                .map(CharityCampaign::getGoalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
