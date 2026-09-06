package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.Vendor;
import io.virinchi.dhammanature.model.enums.Role;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** FR-07 stakeholder onboarding: a user applies to become a Vendor, pending admin verification. */
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    @Transactional
    public Vendor applyAsVendor(User user, String vendorName, String contactDetails, String address) {
        if (vendorRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new IllegalStateException("You already have a vendor account.");
        }
        user.setRole(Role.VENDOR);
        userRepository.save(user);
        return vendorRepository.save(Vendor.builder()
                .vendorName(vendorName).contactDetails(contactDetails).address(address)
                .verified(false).user(user).build());
    }

    public List<Vendor> pendingVerification() {
        return vendorRepository.findAll().stream().filter(v -> !v.isVerified()).toList();
    }

    @Transactional
    public void verify(Integer vendorId) {
        vendorRepository.findById(vendorId).ifPresent(v -> {
            v.setVerified(true);
            vendorRepository.save(v);
        });
    }
}
