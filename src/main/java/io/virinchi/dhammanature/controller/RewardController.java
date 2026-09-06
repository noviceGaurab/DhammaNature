package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.service.RewardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** NFR-04: Reward Redemption - strongly requested by the Ashok Stupa visitors. */
@Controller
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/rewards")
    public String rewards(HttpSession session, Model model) {
        var user = sessionUserResolver.resolve(session);
        model.addAttribute("catalog", rewardService.catalog());
        user.ifPresent(u -> model.addAttribute("history", rewardService.historyFor(u.getId())));
        return "rewards";
    }

    @PostMapping("/rewards/{id}/redeem")
    public String redeem(@org.springframework.web.bind.annotation.PathVariable Integer id,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        rewardService.redeem(user, id);
                        redirectAttributes.addFlashAttribute("success", "Reward redeemed! Show your account at the meditation center to claim it.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/rewards";
                })
                .orElse("redirect:/login");
    }
}
