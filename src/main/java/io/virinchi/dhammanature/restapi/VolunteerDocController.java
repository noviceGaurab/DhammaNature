package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.repository.VolunteerRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves volunteer verification evidence (student ID card, college approval letter) from BLOBs for the admin review screen. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/volunteer-registrations")
public class VolunteerDocController {

    private final VolunteerRegistrationRepository registrationRepository;

    @GetMapping("/{id}/student-id/image")
    public ResponseEntity<byte[]> studentId(@PathVariable Integer id) {
        return registrationRepository.findById(id)
                .filter(r -> r.getStudentIdImageData() != null)
                .map(r -> ResponseEntity.ok()
                        .contentType(r.getStudentIdImageContentType() != null
                                ? MediaType.parseMediaType(r.getStudentIdImageContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(r.getStudentIdImageData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/college-approval/image")
    public ResponseEntity<byte[]> collegeApproval(@PathVariable Integer id) {
        return registrationRepository.findById(id)
                .filter(r -> r.getCollegeApprovalImageData() != null)
                .map(r -> ResponseEntity.ok()
                        .contentType(r.getCollegeApprovalImageContentType() != null
                                ? MediaType.parseMediaType(r.getCollegeApprovalImageContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(r.getCollegeApprovalImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
