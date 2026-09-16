package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.VolunteerOpportunity;
import io.virinchi.dhammanature.model.VolunteerRegistration;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.VolunteerStatus;
import io.virinchi.dhammanature.repository.VolunteerOpportunityRepository;
import io.virinchi.dhammanature.repository.VolunteerRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** NFR-06: Volunteer Registration - food distribution, cleaning, tree plantation, religious events. */
@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerOpportunityRepository opportunityRepository;
    private final VolunteerRegistrationRepository registrationRepository;
    private final RewardService rewardService;
    private final NotificationService notificationService;

    public List<VolunteerOpportunity> upcoming() {
        return opportunityRepository.findByOpportunityDateGreaterThanEqualOrderByOpportunityDateAsc(LocalDate.now());
    }

    public List<VolunteerOpportunity> all() {
        return opportunityRepository.findAllByOrderByOpportunityDateAsc();
    }

    @Transactional
    public VolunteerRegistration register(User user, Integer opportunityId) {
        VolunteerOpportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NoSuchElementException("Volunteer opportunity not found"));

        boolean already = registrationRepository.findByUser_Id(user.getId()).stream()
                .anyMatch(r -> r.getOpportunity().getId().equals(opportunityId));
        if (already) {
            throw new IllegalStateException("You're already registered for \"" + opportunity.getTitle() + "\".");
        }

        VolunteerRegistration registration = registrationRepository.save(VolunteerRegistration.builder()
                .user(user).opportunity(opportunity).status(VolunteerStatus.REGISTERED).build());

        rewardService.awardPoints(user, 15, "Volunteered for \"" + opportunity.getTitle() + "\"");
        notificationService.notifyUser(user, "Volunteer registration confirmed",
                "You're signed up for \"" + opportunity.getTitle() + "\" on " + opportunity.getOpportunityDate() + ".",
                NotificationType.VOLUNTEER);
        return registration;
    }

    public List<VolunteerRegistration> forUser(Integer userId) {
        return registrationRepository.findByUser_Id(userId);
    }

    public List<VolunteerRegistration> forOpportunity(Integer opportunityId) {
        return registrationRepository.findByOpportunity_Id(opportunityId);
    }

    public List<VolunteerOpportunity> forCenter(Integer centerId) {
        return opportunityRepository.findByMeditationCenter_Id(centerId);
    }
}
