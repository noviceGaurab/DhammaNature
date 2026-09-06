package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.CharityCampaign;
import io.virinchi.dhammanature.model.Donation;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.DonationType;
import io.virinchi.dhammanature.repository.CharityCampaignRepository;
import io.virinchi.dhammanature.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** FR-05: Donation Management. Donations cannot be edited once completed (Business Rule 5). */
@Service
@RequiredArgsConstructor
public class DonationService {

    private static final BigDecimal POINTS_PER_CURRENCY_UNIT = new BigDecimal("0.5"); // 1 point per $2 donated

    private final DonationRepository donationRepository;
    private final CharityCampaignRepository charityCampaignRepository;
    private final RewardService rewardService;

    @Transactional
    public Donation donate(User user, Integer campaignId, String firstName, String lastName, String email,
                            BigDecimal amount, String paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Donation amount must be greater than zero.");
        }
        CharityCampaign campaign = null;
        DonationType type = DonationType.GENERAL;
        if (campaignId != null) {
            campaign = charityCampaignRepository.findById(campaignId).orElse(null);
            type = DonationType.CAMPAIGN;
        }

        Donation donation = donationRepository.save(Donation.builder()
                .user(user)
                .campaign(campaign)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .amount(amount)
                .donationType(type)
                .paymentMethod(paymentMethod)
                .build());

        if (campaign != null) {
            campaign.addRaisedAmount(amount);
            charityCampaignRepository.save(campaign);
        }

        if (user != null) {
            int points = amount.multiply(POINTS_PER_CURRENCY_UNIT).intValue();
            if (points > 0) {
                rewardService.awardPoints(user, points, "Donation of " + amount + " - thank you for your generosity");
            }
        }
        return donation;
    }

    public List<Donation> all() {
        return donationRepository.findAllByOrderByDonationDateDesc();
    }

    public List<Donation> forUser(Integer userId) {
        return donationRepository.findByUser_IdOrderByDonationDateDesc(userId);
    }

    public BigDecimal totalDonated() {
        return donationRepository.totalDonated();
    }
}
