package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.CharityCampaign;
import io.virinchi.dhammanature.model.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CharityCampaignRepository extends JpaRepository<CharityCampaign, Integer> {
    @Query("SELECT cc FROM CharityCampaign cc JOIN FETCH cc.meditationCenter WHERE cc.status = :status")
    List<CharityCampaign> findByStatusWithMeditationCenter(@Param("status") CampaignStatus status);
    List<CharityCampaign> findByStatus(io.virinchi.dhammanature.model.enums.CampaignStatus status);
    List<CharityCampaign> findByMeditationCenter_Id(Integer centerId);
}
