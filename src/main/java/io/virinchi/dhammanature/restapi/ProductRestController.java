package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.dto.ProductCreateRequest;
import io.virinchi.dhammanature.model.Product;
import io.virinchi.dhammanature.model.Vendor;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import io.virinchi.dhammanature.repository.ProductRepository;
import io.virinchi.dhammanature.repository.VendorRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final ApiAuth apiAuth;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll().stream().map(ApiViews::product).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Integer id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> ApiException.notFound("Product " + id + " not found"));
        return ResponseEntity.ok(ApiViews.product(product));
    }

    /** Admin only: lists a new product. */
    @PostMapping
    public ResponseEntity<?> saveProduct(@Valid @RequestBody ProductCreateRequest request, HttpSession session) {
        apiAuth.requireAdmin(session);
        Vendor vendor = vendorRepository.findByVerifiedTrue().stream().findFirst().orElseThrow(
                () -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "No verified vendor available to list this product."));

        Product product = Product.builder()
                .vendor(vendor)
                .productName(request.name().trim())
                .description(request.description())
                .price(request.price())
                .stockQuantity(0)
                .imageUrl("")
                .category(ProductCategory.HANDICRAFTS)
                .build();

        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.product(saved));
    }
}
