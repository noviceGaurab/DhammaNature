package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.CharityCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharityCampaignRepository extends JpaRepository<CharityCampaign, Integer> {
    List<CharityCampaign> findByStatus(io.virinchi.dhammanature.model.enums.CampaignStatus status);
    List<CharityCampaign> findByMeditationCenter_Id(Integer centerId);
}
