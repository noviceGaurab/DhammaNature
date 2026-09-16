package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.VolunteerOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VolunteerOpportunityRepository extends JpaRepository<VolunteerOpportunity, Integer> {
    @Query("SELECT o FROM VolunteerOpportunity o JOIN FETCH o.meditationCenter " +
            "WHERE o.opportunityDate >= :date ORDER BY o.opportunityDate ASC")
    List<VolunteerOpportunity> findByOpportunityDateGreaterThanEqualOrderByOpportunityDateAsc(java.time.LocalDate date);
    List<VolunteerOpportunity> findAllByOrderByOpportunityDateAsc();
    List<VolunteerOpportunity> findByMeditationCenter_Id(Integer centerId);
}
