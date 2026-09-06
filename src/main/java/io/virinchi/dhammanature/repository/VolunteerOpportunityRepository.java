package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.VolunteerOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerOpportunityRepository extends JpaRepository<VolunteerOpportunity, Integer> {
    List<VolunteerOpportunity> findByOpportunityDateGreaterThanEqualOrderByOpportunityDateAsc(java.time.LocalDate date);
    List<VolunteerOpportunity> findAllByOrderByOpportunityDateAsc();
}
