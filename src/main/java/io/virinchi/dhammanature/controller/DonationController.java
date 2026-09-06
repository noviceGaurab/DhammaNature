package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.Donation;
import io.virinchi.dhammanature.service.CharityCampaignService;
import io.virinchi.dhammanature.service.DonationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** FR-05: Donation Management. Business Rule 5: donations cannot be edited once completed. */
@Controller
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final CharityCampaignService charityCampaignService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/donate")
    public String donatePage(@RequestParam(required = false) Integer campaignId, Model model) {
        model.addAttribute("campaigns", charityCampaignService.active());
        model.addAttribute("selectedCampaignId", campaignId);
        model.addAttribute("totalDonated", donationService.totalDonated());
        return "donate";
    }

    @PostMapping("/donate")
    public String donate(@RequestParam(required = false) Integer campaignId,
                          @RequestParam String firstName,
                          @RequestParam(required = false) String lastName,
                          @RequestParam(required = false) String email,
                          @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String paymentMethod,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        try {
            Donation donation = donationService.donate(user, campaignId, firstName, lastName, email, amount,
                    (paymentMethod == null || paymentMethod.isBlank()) ? "Card" : paymentMethod);
            redirectAttributes.addFlashAttribute("donation", donation);
            return "redirect:/donate/confirm?receipt=" + donation.getReceiptNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/donate";
        }
    }

    @GetMapping("/donate/confirm")
    public String confirm() {
        return "donate-confirm";
    }
}
