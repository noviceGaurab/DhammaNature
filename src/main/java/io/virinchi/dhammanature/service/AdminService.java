package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.MeditationCenter;
import io.virinchi.dhammanature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Administrator responsibilities from Section 4.1: manage users, approve
 * meditation centers, monitor donations, verify vendors, moderate discussions.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MeditationCenterRepository meditationCenterRepository;
    private final DonationRepository donationRepository;
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
}
