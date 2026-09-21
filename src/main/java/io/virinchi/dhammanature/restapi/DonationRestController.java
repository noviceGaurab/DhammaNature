package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.service.CharityCampaignService;
import io.virinchi.dhammanature.service.DonationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donations")
public class DonationRestController {

    private final DonationService donationService;
    private final CharityCampaignService charityCampaignService;
    private final ApiAuth apiAuth;

    @GetMapping("/campaigns")
    public ResponseEntity<?> campaigns() {
        return ResponseEntity.ok(charityCampaignService.active().stream().map(ApiViews::campaign).toList());
    }

    /** Donations are open to guests (no session required) and to logged-in members. */
    @PostMapping
    public ResponseEntity<?> donate(@Valid @RequestBody DonationRequest request, HttpSession session) {
        var user = apiAuth.currentUser(session);
        var donation = donationService.donate(user, request.campaignId(),
                request.firstName(), request.lastName(), request.email(),
                request.amount(), request.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.donation(donation));
    }

    public record DonationRequest(
            @NotNull(message = "Please choose a campaign.")
            Integer campaignId,

            @NotBlank(message = "First name is required.")
            @Size(max = 100, message = "First name is too long.")
            String firstName,

            @Size(max = 100, message = "Last name is too long.")
            String lastName,

            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email,

            @NotNull(message = "Donation amount is required.")
            @DecimalMin(value = "0.0", inclusive = false, message = "Donation amount must be greater than zero.")
            BigDecimal amount,

            @Size(max = 50, message = "Payment method is too long.")
            String paymentMethod) {
    }
}
