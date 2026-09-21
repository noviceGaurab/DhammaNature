package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.UserBlock;
import io.virinchi.dhammanature.model.UserReport;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.Role;
import io.virinchi.dhammanature.repository.UserBlockRepository;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.repository.UserReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Peer-to-peer safety features surfaced on the discuss hover menu: block,
 * unblock and report another participant. Reports carry a written reason and are
 * listed on the admin review page where an admin can resolve them.
 */
@Service
@RequiredArgsConstructor
public class CommunitySafetyService {

    private final UserBlockRepository blockRepository;
    private final UserReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void block(User blocker, Integer targetId) {
        if (targetId == null || targetId.equals(blocker.getId())) {
            throw new IllegalArgumentException("You cannot block yourself.");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));
        if (!blockRepository.existsByBlocker_IdAndBlocked_Id(blocker.getId(), target.getId())) {
            blockRepository.save(UserBlock.builder().blocker(blocker).blocked(target).build());
        }
    }

    @Transactional
    public void unblock(User blocker, Integer targetId) {
        if (targetId != null) {
            blockRepository.deleteByBlocker_IdAndBlocked_Id(blocker.getId(), targetId);
        }
    }

    @Transactional(readOnly = true)
    public Set<Integer> blockedIdsFor(Integer userId) {
        return new HashSet<>(blockRepository.blockedUserIdsBy(userId));
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Integer aId, Integer bId) {
        return blockRepository.existsByBlocker_IdAndBlocked_Id(aId, bId)
                || blockRepository.existsByBlocker_IdAndBlocked_Id(bId, aId);
    }

    @Transactional
    public UserReport report(User reporter, Integer targetId, String reason, String details) {
        if (targetId == null || targetId.equals(reporter.getId())) {
            throw new IllegalArgumentException("You cannot report yourself.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Please choose a reason for the report.");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));
        String trimmedReason = reason.trim();
        String fullReason = (details != null && !details.isBlank())
                ? trimmedReason + "\n\nAdditional details:\n" + details.trim()
                : trimmedReason;
        UserReport report = reportRepository.save(UserReport.builder()
                .reporter(reporter)
                .reported(target)
                .reason(fullReason)
                .build());
        // Alert every administrator - reports are reviewed on the /admin/incidents page.
        userRepository.findByRole(Role.ADMIN).forEach(admin ->
                notificationService.notifyUser(admin, "New community report",
                        reporter.getFullName() + " filed a report against " + target.getFullName()
                                + ":\n" + fullReason + "\n\nReview it under Admin -> Incidents.",
                        NotificationType.USER_REPORT));
        return report;
    }

    public List<UserReport> reports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public long pendingReportCount() {
        return reportRepository.countByStatus(UserReport.Status.PENDING);
    }

    @Transactional
    public void resolveReport(Integer reportId, User admin) {
        UserReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("Report not found"));
        report.setStatus(UserReport.Status.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);
        // Let the reporter know their report was reviewed and resolved.
        notificationService.notifyUser(report.getReporter(), "Your report was reviewed",
                admin.getFullName() + " reviewed your report against " + report.getReported().getFullName()
                        + " and resolved it. Thank you for helping keep the community safe.",
                NotificationType.USER_REPORT);
    }
}