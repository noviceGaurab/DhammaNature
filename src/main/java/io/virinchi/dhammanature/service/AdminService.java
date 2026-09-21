package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.*;
import io.virinchi.dhammanature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Administrator responsibilities from Section 4.1: manage users, approve
 * meditation centers, monitor donations, verify vendors, moderate discussions.
 * Hides the repositories behind one service so the admin controllers do not
 * depend on Spring Data directly.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MeditationCenterRepository meditationCenterRepository;
    private final DonationRepository donationRepository;
    private final CommentRepository commentRepository;
    private final VendorRepository vendorRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final BookingRepository bookingRepository;
    private final ProductOrderRepository productOrderRepository;

    public List<MeditationCenter> pendingCenters() {
        return meditationCenterRepository.findAll().stream().filter(c -> !c.isVerified()).toList();
    }

    @Transactional
    public void verifyCenter(Integer centerId) {
        meditationCenterRepository.findById(centerId).ifPresent(c -> {
            c.setVerified(true);
            meditationCenterRepository.save(c);
        });
    }

    public long totalUsers() {
        return userRepository.count();
    }

    public long totalCenters() {
        return meditationCenterRepository.count();
    }

    public long totalDonations() {
        return donationRepository.count();
    }

    public long totalBookings() {
        return bookingRepository.count();
    }

    public long totalOrders() {
        return productOrderRepository.count();
    }

    // ===== Admin panel listings =====

    public Page<User> pagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /** All users, for the reports aggregation. */
    public List<User> allUsers() {
        return userRepository.findAll();
    }

    /** All donations, for the reports aggregation and the admin donations screen. */
    public List<Donation> allDonations() {
        return donationRepository.findAll();
    }

    public List<Donation> recentDonations() {
        return donationRepository.findAllByOrderByDonationDateDesc();
    }

    public List<Comment> allComments() {
        return commentRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<MeditationCenter> allCenters() {
        return meditationCenterRepository.findAll();
    }

    public List<Vendor> allVendors() {
        return vendorRepository.findAll();
    }

    public long quizAttemptCount(Integer quizId) {
        return quizAttemptRepository.countByQuiz_Id(quizId);
    }

    /**
     * Soft-deletes a user from the admin panel: the account is marked inactive so
     * they can no longer sign in, but every related record (bookings, donations,
     * comments, reports, etc.) is preserved so the person can be unblocked again.
     */
    @Transactional
    public void setUserActive(Integer userId, boolean active) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setActive(active);
            userRepository.save(u);
        });
    }
}
