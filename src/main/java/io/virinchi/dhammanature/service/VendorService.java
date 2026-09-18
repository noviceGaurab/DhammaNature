package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.Vendor;
import io.virinchi.dhammanature.model.VendorDocument;
import io.virinchi.dhammanature.model.enums.Role;
import io.virinchi.dhammanature.model.enums.VendorDocType;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.repository.VendorDocumentRepository;
import io.virinchi.dhammanature.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** FR-07 stakeholder onboarding: a user applies to become a Vendor, pending admin verification. */
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final VendorDocumentRepository vendorDocumentRepository;

    public Optional<Vendor> findByUser(Integer userId) {
        return vendorRepository.findByUser_Id(userId);
    }

    @Transactional
    public Vendor applyAsVendor(User user, String vendorName, String contactDetails, String address) {
        return applyAsVendor(user, vendorName, contactDetails, address, List.of());
    }

    @Transactional
    public Vendor applyAsVendor(User user, String vendorName, String contactDetails, String address,
                                List<VendorDocument> documents) {
        if (vendorRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new IllegalStateException("You already have a vendor account.");
        }
        user.setRole(Role.VENDOR);
        userRepository.save(user);
        Vendor vendor = vendorRepository.save(Vendor.builder()
                .vendorName(vendorName).contactDetails(contactDetails).address(address)
                .verified(false).user(user).build());
        if (documents != null && !documents.isEmpty()) {
            documents.forEach(d -> d.setVendor(vendor));
            vendorDocumentRepository.saveAll(documents);
            vendor.setDocuments(new ArrayList<>(documents));
        }
        return vendor;
    }

    public List<Vendor> pendingVerification() {
        return vendorRepository.findAll().stream().filter(v -> !v.isVerified()).toList();
    }

    /** Evidence checklist status, e.g. how many of each document type were supplied. */
    public long documentCount(Integer vendorId) {
        return vendorDocumentRepository.findByVendor_IdOrderByIdAsc(vendorId).size();
    }

    public List<VendorDocument> documents(Integer vendorId) {
        return vendorDocumentRepository.findByVendor_IdOrderByIdAsc(vendorId);
    }

    /** True when every core evidence type has at least one uploaded file - shown to admins as a readiness signal. */
    public boolean hasCompleteEvidence(Integer vendorId) {
        List<VendorDocType> required = List.of(VendorDocType.BUSINESS_REGISTRATION,
                VendorDocType.TAX_ID, VendorDocType.SHOP_LOCATION, VendorDocType.IDENTITY);
        List<VendorDocType> present = documents(vendorId).stream().map(VendorDocument::getDocType).toList();
        return present.containsAll(required);
    }

    @Transactional
    public void verify(Integer vendorId, User admin) {
        requireAdmin(admin);
        vendorRepository.findById(vendorId).ifPresent(v -> {
            v.setVerified(true);
            vendorRepository.save(v);
        });
    }

    @Transactional
    public void reject(Integer vendorId, User admin) {
        requireAdmin(admin);
        vendorRepository.findById(vendorId).ifPresent(v -> {
            v.setVerified(false);
            vendorRepository.save(v);
        });
    }

    /** Only an ADMIN may approve or reject a vendor's evidence documents. */
    private void requireAdmin(User admin) {
        if (admin == null || admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only an administrator may verify or reject vendor documents.");
        }
    }
}
