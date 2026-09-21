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

    public List<VolunteerRegistration> allRegistrations() {
        return registrationRepository.findAll();
    }

    /**
     * Step 2 of the verified sign-up flow: an existing record created in the warning step
     * is completed with the student's ID card and college approval evidence.
     */
    @Transactional
    public VolunteerRegistration registerVerified(User user, Integer opportunityId, String studentIdNumber,
                                                  byte[] studentIdImageData, String studentIdImageContentType,
                                                  byte[] collegeApprovalImageData, String collegeApprovalImageContentType,
                                                  String collegeName) {
        VolunteerOpportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NoSuchElementException("Volunteer opportunity not found"));

        List<VolunteerRegistration> mine = registrationRepository.findByUser_Id(user.getId());
        boolean already = mine.stream()
                .anyMatch(r -> r.getOpportunity().getId().equals(opportunityId)
                        && r.getStatus() != VolunteerStatus.CANCELLED
                        && r.getStatus() != VolunteerStatus.REJECTED);
        if (already) {
            throw new IllegalStateException("You're already registered for \"" + opportunity.getTitle() + "\".");
        }

        if (studentIdNumber == null || studentIdNumber.isBlank()) {
            throw new IllegalStateException("Please provide your student ID number so we can verify you as a student.");
        }
        if (studentIdImageData == null || studentIdImageData.length == 0) {
            throw new IllegalStateException("Please upload a photo of your student ID card.");
        }
        if (collegeApprovalImageData == null || collegeApprovalImageData.length == 0) {
            throw new IllegalStateException("Please upload the signed college approval letter (with the college logo / letterhead).");
        }

        VolunteerRegistration registration = registrationRepository.save(VolunteerRegistration.builder()
                .user(user).opportunity(opportunity).status(VolunteerStatus.PENDING_VERIFICATION)
                .studentIdNumber(studentIdNumber.trim())
                .studentIdImageData(studentIdImageData)
                .studentIdImageContentType(studentIdImageContentType)
                .collegeApprovalImageData(collegeApprovalImageData)
                .collegeApprovalImageContentType(collegeApprovalImageContentType)
                .collegeName(collegeName == null || collegeName.isBlank() ? null : collegeName.trim())
                .warningsAccepted(true)
                .build());

        notificationService.notifyUser(user, "Volunteer verification pending",
                "We received your application for \"" + opportunity.getTitle() + "\" on " + opportunity.getOpportunityDate()
                        + ". A coordinator will review your student ID and college approval letter before your seat is confirmed.",
                NotificationType.VOLUNTEER);
        return registration;
    }

    /** Admin approves a volunteer's verified documents - seat confirmed + reward points credited. */
    @Transactional
    public VolunteerRegistration approve(Integer registrationId, User admin) {
        requireAdmin(admin);
        VolunteerRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NoSuchElementException("Volunteer registration not found"));
        if (registration.getStatus() == VolunteerStatus.CONFIRMED) {
            return registration;
        }
        registration.setStatus(VolunteerStatus.CONFIRMED);
        registrationRepository.save(registration);
        rewardService.awardPoints(registration.getUser(), 15,
                "Volunteer verified & confirmed for \"" + registration.getOpportunity().getTitle() + "\"");
        notificationService.notifyUser(registration.getUser(),
                "Volunteer seat confirmed",
                "Your documents were verified. You're confirmed for \"" + registration.getOpportunity().getTitle()
                        + "\" on " + registration.getOpportunity().getOpportunityDate()
                        + ". Remember: if you can no longer attend, email us at least 24 hours in advance.",
                NotificationType.VOLUNTEER);
        return registration;
    }

    /** Admin rejects a volunteer whose documents cannot be verified. */
    @Transactional
    public VolunteerRegistration reject(Integer registrationId, User admin) {
        requireAdmin(admin);
        VolunteerRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new NoSuchElementException("Volunteer registration not found"));
        registration.setStatus(VolunteerStatus.REJECTED);
        registrationRepository.save(registration);
        notificationService.notifyUser(registration.getUser(),
                "Volunteer application not verified",
                "We couldn't verify the documents for \"" + registration.getOpportunity().getTitle()
                        + "\". You can sign up again with a valid student ID and a college approval letter on official letterhead.",
                NotificationType.VOLUNTEER);
        return registration;
    }

    public List<VolunteerRegistration> pendingVerifications() {
        return registrationRepository.findByStatusOrderByRegisteredAtDesc(VolunteerStatus.PENDING_VERIFICATION);
    }

    public long pendingCount() {
        return registrationRepository.countByStatus(VolunteerStatus.PENDING_VERIFICATION);
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

    /** Only an ADMIN may approve or reject a volunteer's verification documents. */
    private void requireAdmin(User admin) {
        if (admin == null || admin.getRole() != io.virinchi.dhammanature.model.enums.Role.ADMIN) {
            throw new IllegalStateException("Only an administrator may approve or reject volunteer documents.");
        }
    }
}
