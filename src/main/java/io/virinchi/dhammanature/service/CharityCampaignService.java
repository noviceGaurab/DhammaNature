package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.CharityCampaign;
import io.virinchi.dhammanature.model.MeditationCenter;
import io.virinchi.dhammanature.model.enums.CampaignStatus;
import io.virinchi.dhammanature.repository.CharityCampaignRepository;
import io.virinchi.dhammanature.repository.MeditationCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** NFR-05: Charity Campaign Module. Business Rule 8: only verified organizations may create campaigns. */
@Service
@RequiredArgsConstructor
public class CharityCampaignService {

    private final CharityCampaignRepository charityCampaignRepository;
    private final MeditationCenterRepository meditationCenterRepository;

    public List<CharityCampaign> active() {
        return charityCampaignRepository.findByStatus(CampaignStatus.ACTIVE);
    }

    public List<CharityCampaign> all() {
        return charityCampaignRepository.findAll();
    }

    public CharityCampaign get(Integer id) {
        return charityCampaignRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Charity campaign not found"));
    }

    public CharityCampaign create(Integer centerId, String title, String description,
                                   BigDecimal goalAmount, LocalDate startDate, LocalDate endDate) {
        MeditationCenter center = meditationCenterRepository.findById(centerId)
                .orElseThrow(() -> new NoSuchElementException("Meditation center not found"));
        if (!center.isVerified()) {
            throw new IllegalStateException("Only verified meditation centers may create charity campaigns.");
        }
        return charityCampaignRepository.save(CharityCampaign.builder()
                .meditationCenter(center).title(title).description(description)
                .goalAmount(goalAmount).startDate(startDate).endDate(endDate)
                .status(CampaignStatus.ACTIVE).build());
    }
}
