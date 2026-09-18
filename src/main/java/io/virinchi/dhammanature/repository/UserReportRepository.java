package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Integer> {

    List<UserReport> findAllByOrderByCreatedAtDesc();

    long countByStatus(UserReport.Status status);
}