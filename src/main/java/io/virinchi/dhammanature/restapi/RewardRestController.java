package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.service.RewardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardRestController {

    private final RewardService rewardService;
    private final ApiAuth apiAuth;

    @GetMapping
    public ResponseEntity<?> catalog() {
        return ResponseEntity.ok(rewardService.catalog().stream().map(ApiViews::rewardCatalogItem).toList());
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(HttpSession session) {
        var user = apiAuth.requireUser(session);
        return ResponseEntity.ok(rewardService.historyFor(user.getId()).stream()
                .map(ApiViews::rewardTransaction).toList());
    }

    @PostMapping("/{id}/redeem")
    public ResponseEntity<?> redeem(@PathVariable Integer id, HttpSession session) {
        var user = apiAuth.requireUser(session);
        var tx = rewardService.redeem(user, id);
        return ResponseEntity.ok(Map.of(
                "message", "Reward redeemed! Show your account at the meditation center to claim it.",
                "pointsRemaining", user.getRewardPoints(),
                "transaction", ApiViews.rewardTransaction(tx)));
    }
}
