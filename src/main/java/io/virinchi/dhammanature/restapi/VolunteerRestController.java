package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.config.ImageRules;
import io.virinchi.dhammanature.service.VolunteerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/volunteer")
public class VolunteerRestController {

    private final VolunteerService volunteerService;
    private final ApiAuth apiAuth;

    @GetMapping("/opportunities")
    public ResponseEntity<?> opportunities() {
        return ResponseEntity.ok(volunteerService.upcoming().stream()
                .map(ApiViews::volunteerOpportunity).toList());
    }

    @PostMapping(value = "/opportunities/{id}/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@PathVariable Integer id,
                                      @RequestParam String studentIdNumber,
                                      @RequestParam(required = false) String collegeName,
                                      @RequestParam("studentIdImage") MultipartFile studentIdImage,
                                      @RequestParam("collegeApprovalImage") MultipartFile collegeApprovalImage,
                                      HttpSession session) {
        var user = apiAuth.requireUser(session);
        String studentIdType = ImageRules.requireContentType(studentIdImage, "student ID card photo");
        byte[] studentIdBytes = ImageRules.bytes(studentIdImage);
        String approvalType = ImageRules.requireContentType(collegeApprovalImage,
                "college approval letter (with official logo/letterhead)");
        byte[] approvalBytes = ImageRules.bytes(collegeApprovalImage);
        var registration = volunteerService.registerVerified(user, id, studentIdNumber,
                studentIdBytes, studentIdType, approvalBytes, approvalType, collegeName);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.volunteerRegistration(registration));
    }
}