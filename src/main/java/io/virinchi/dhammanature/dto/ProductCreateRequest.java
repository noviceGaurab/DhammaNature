package io.virinchi.dhammanature.dto;

import java.math.BigDecimal;

/**
 * JSON body accepted by POST /api/products, matching the course spec:
 * <pre>{ "name": "...", "description": "...", "price": 9.99 }</pre>
 */
public record ProductCreateRequest(String name, String description, BigDecimal price) {
}