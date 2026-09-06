package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.RewardCatalogItem;
import io.virinchi.dhammanature.model.RewardTransaction;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.RewardTransactionType;
import io.virinchi.dhammanature.repository.RewardCatalogItemRepository;
import io.virinchi.dhammanature.repository.RewardTransactionRepository;
import io.virinchi.dhammanature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * FR-09 / NFR-04: Reward System + Reward Redemption. Directly and strongly
 * validated by the Ashok Stupa interviews (Section 4.5.2, requirement NFR-04):
 * users earn points via participation and redeem them for incense coupons,
 * meditation discounts, charity sponsorship or certificates.
 */
@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardTransactionRepository rewardTransactionRepository;
    private final RewardCatalogItemRepository rewardCatalogItemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void awardPoints(User user, int points, String reason) {
        if (points <= 0) return;
        user.setRewardPoints(user.getRewardPoints() + points);
        userRepository.save(user);
        rewardTransactionRepository.save(RewardTransaction.builder()
                .user(user).type(RewardTransactionType.EARNED).points(points).reason(reason).build());
        notificationService.notifyUser(user, "You earned " + points + " reward points",
                reason, NotificationType.REWARD);
    }

    /** Business Rule 9: users may redeem points only when sufficient points are available. */
    @Transactional
    public RewardTransaction redeem(User user, Integer catalogItemId) {
        RewardCatalogItem item = rewardCatalogItemRepository.findById(catalogItemId)
                .orElseThrow(() -> new NoSuchElementException("Reward item not found"));
        if (!item.isActive()) {
            throw new IllegalStateException("This reward is no longer available.");
        }
        if (user.getRewardPoints() < item.getPointsCost()) {
            throw new IllegalStateException("Not enough reward points to redeem \"" + item.getName() + "\".");
        }
        user.setRewardPoints(user.getRewardPoints() - item.getPointsCost());
        userRepository.save(user);
        RewardTransaction tx = rewardTransactionRepository.save(RewardTransaction.builder()
                .user(user)
                .type(RewardTransactionType.REDEEMED)
                .points(-item.getPointsCost())
                .reason("Redeemed: " + item.getName())
                .redeemedItem(item)
                .build());
        notificationService.notifyUser(user, "Reward redeemed: " + item.getName(),
                "You spent " + item.getPointsCost() + " points. Show this in your account at the meditation center to claim it.",
                NotificationType.REWARD);
        return tx;
    }

    public List<RewardCatalogItem> catalog() {
        return rewardCatalogItemRepository.findByActiveTrue();
    }

    public List<RewardTransaction> historyFor(Integer userId) {
        return rewardTransactionRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }
}
