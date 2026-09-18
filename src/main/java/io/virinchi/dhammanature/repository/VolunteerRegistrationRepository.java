package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.VolunteerRegistration;
import io.virinchi.dhammanature.model.enums.VolunteerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerRegistrationRepository extends JpaRepository<VolunteerRegistration, Integer> {
    List<VolunteerRegistration> findByUser_Id(Integer userId);
    List<VolunteerRegistration> findByOpportunity_Id(Integer opportunityId);
    List<VolunteerRegistration> findByStatusOrderByRegisteredAtDesc(VolunteerStatus status);
    long countByStatus(VolunteerStatus status);
}
