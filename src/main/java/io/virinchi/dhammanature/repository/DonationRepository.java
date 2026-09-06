package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Integer> {
    List<Donation> findAllByOrderByDonationDateDesc();
    List<Donation> findByUser_IdOrderByDonationDateDesc(Integer userId);
    List<Donation> findByCampaign_Id(Integer campaignId);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(d.amount),0) from Donation d")
    java.math.BigDecimal totalDonated();
}
