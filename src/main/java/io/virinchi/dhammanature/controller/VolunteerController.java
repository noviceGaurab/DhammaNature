package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.config.StorageConfig;
import io.virinchi.dhammanature.service.VolunteerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** NFR-06: Volunteer Registration - food distribution, cleaning, tree plantation, religious events.
 *  Step 1 shows the warnings / future-opportunity + no-show punishment rules; second step asks for
 *  a student ID card and a college approval letter (with official logo/letterhead) so only genuine
 *  students are given a spot, then an admin verifies the documents before the seat is confirmed. */
@Controller
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;
    private final StorageConfig storageConfig;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/volunteer")
    public String list(HttpSession session, Model model) {
        model.addAttribute("opportunities", volunteerService.upcoming());
        model.addAttribute("pendingCount", volunteerService.pendingCount());
        sessionUserResolver.resolve(session).ifPresent(u ->
                model.addAttribute("myRegistrations", volunteerService.forUser(u.getId())));
        return "volunteer";
    }

    /** Step 1 - read the warning: future opportunities you qualify for, and the punishment for a no-show. */
    @GetMapping("/volunteer/{id}/register")
    public String warnings(@PathVariable Integer id, HttpSession session, Model model) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    model.addAttribute("opportunity", volunteerService.all().stream()
                            .filter(o -> o.getId().equals(id))
                            .findFirst()
                            .orElseThrow(() -> new java.util.NoSuchElementException("Volunteer opportunity not found")));
                    return "volunteer-register";
                })
                .orElse("redirect:/login");
    }

    /** Step 2 - submit student ID card + college approval letter (logo/letterhead) for verification. */
    @PostMapping("/volunteer/{id}/register")
    public String register(@PathVariable Integer id,
                           @RequestParam(value = "warningsAccepted", required = false) String warningsAccepted,
                           @RequestParam(required = false) String studentIdNumber,
                           @RequestParam(required = false) String collegeName,
                           @RequestParam(required = false) MultipartFile studentIdImage,
                           @RequestParam(required = false) MultipartFile collegeApprovalImage,
                           HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    var opportunity = volunteerService.all().stream()
                            .filter(o -> o.getId().equals(id))
                            .findFirst()
                            .orElseThrow(() -> new java.util.NoSuchElementException("Volunteer opportunity not found"));
                    if (warningsAccepted == null || !"on".equalsIgnoreCase(warningsAccepted)) {
                        model.addAttribute("opportunity", opportunity);
                        model.addAttribute("error", "Please read and accept the volunteer commitment first - it explains the no-show rules.");
                        return "volunteer-register";
                    }
                    try {
                        String studentIdImageName = saveImage(studentIdImage, "Student ID card photo");
                        String approvalImageName = saveImage(collegeApprovalImage, "College approval letter (with official logo/letterhead)");
                        volunteerService.registerVerified(user, id, studentIdNumber, studentIdImageName,
                                approvalImageName, collegeName);
                        redirectAttributes.addFlashAttribute("success",
                                "Application sent! A coordinator will verify your student ID and college approval letter before your seat is confirmed.");
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        model.addAttribute("opportunity", opportunity);
                        model.addAttribute("error", e.getMessage());
                        return "volunteer-register";
                    }
                    return "redirect:/volunteer";
                })
                .orElse("redirect:/login");
    }

    private String saveImage(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload the " + label + ".");
        }
        return storageConfig.saveImage(file);
    }
}