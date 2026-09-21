package io.virinchi.dhammanature.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body accepted by POST /api/auth/signup:
 * <pre>{ "email": "...", "password": "...", "fullName": "optional", "genderIdentity": "optional", "acceptTerms": true }</pre>
 */
public record SignupRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        @Size(max = 200, message = "Email is too long.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters.")
        String password,

        @Size(max = 150, message = "Full name is too long.")
        String fullName,

        @Size(max = 100, message = "Gender/pronoun field is too long.")
        String genderIdentity,

        @NotNull(message = "You must accept the Terms of Service.")
        @AssertTrue(message = "You must accept the Terms of Service.")
        Boolean acceptTerms) {
}
