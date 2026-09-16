package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.CharityCampaign;
import io.virinchi.dhammanature.model.Donation;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.DonationType;
import io.virinchi.dhammanature.repository.CharityCampaignRepository;
import io.virinchi.dhammanature.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailService emailService;

    @Value("${app.base-url:}")
    private String baseUrl;

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

        sendReceiptEmail(donation, campaign, firstName, lastName, email);
        return donation;
    }

    private void sendReceiptEmail(Donation donation, CharityCampaign campaign, String firstName,
                                  String lastName, String email) {
        String donorEmail = (email != null && !email.isBlank()) ? email.trim()
                : (donation.getUser() != null ? donation.getUser().getEmail() : null);
        if (donorEmail != null && !donorEmail.isBlank()) {
            emailService.send(donorEmail, "Donation receipt - Dhamma Nature",
                    "Dear " + firstName + " " + (lastName == null ? "" : lastName) + ",\n\n"
                            + "Thank you for your generous donation of $" + donation.getAmount() + ".\n"
                            + "Your contribution supports Dhamma Nature's teachings, retreats and community service.\n\n"
                            + "Receipt number: " + donation.getReceiptNumber() + "\n"
                            + "Campaign: " + (campaign != null ? campaign.getTitle() : "General donation") + "\n"
                            + "Payment method: " + donation.getPaymentMethod() + "\n"
                            + "Date: " + donation.getDonationDate() + "\n\n"
                            + "View or print your receipt here: " + baseUrl + "/donate/confirm?receipt="
                            + donation.getReceiptNumber() + "\n\n"
                            + "May your generosity bring happiness to you and to all beings. Sadhu!\n\n"
                            + "With metta,\nThe Dhamma Nature team");
        }
        emailService.sendSiteAlert("New donation: " + donation.getAmount(),
                (firstName + " " + (lastName == null ? "" : lastName)).trim() + " donated "
                        + donation.getAmount()
                        + (campaign != null ? " to \"" + campaign.getTitle() + "\"" : " as a general donation")
                        + " (receipt " + donation.getReceiptNumber() + ").");
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
