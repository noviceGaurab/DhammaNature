package io.virinchi.dhammanature.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * JSON body accepted by POST /api/products, matching the course spec:
 * <pre>{ "name": "...", "description": "...", "price": 9.99 }</pre>
 */
public record ProductCreateRequest(
        @NotBlank(message = "A product name is required.")
        @Size(max = 200, message = "Product name must be 200 characters or fewer.")
        String name,

        @Size(max = 4000, message = "Description must be 4000 characters or fewer.")
        String description,

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
        BigDecimal price) {
}
