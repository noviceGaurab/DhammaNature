package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.dto.ProductCreateRequest;
import io.virinchi.dhammanature.model.Product;
import io.virinchi.dhammanature.model.Vendor;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import io.virinchi.dhammanature.repository.ProductRepository;
import io.virinchi.dhammanature.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Integer id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok((Object) product))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(error("Product " + id + " not found")));
    }

    @PostMapping
    public ResponseEntity<?> saveProduct(@RequestBody ProductCreateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().body(error("A product name is required."));
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(error("Price must be greater than zero."));
        }

        Vendor vendor = vendorRepository.findByVerifiedTrue().stream().findFirst().orElse(null);
        if (vendor == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(error("No verified vendor available to list this product."));
        }

        Product product = Product.builder()
                .vendor(vendor)
                .productName(request.name().trim())
                .description(request.description())
                .price(request.price())
                .stockQuantity(0)
                .imageUrl("")
                .category(ProductCategory.HANDICRAFTS)
                .build();

        try {
            Product saved = productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(error("Could not save product - invalid data."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return body;
    }
}