package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductImageController {

    private final ProductRepository productRepository;

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        return productRepository.findById(id)
                .filter(p -> p.getImageData() != null)
                .map(p -> ResponseEntity.ok()
                        .contentType(p.getImageContentType() != null
                                ? MediaType.parseMediaType(p.getImageContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(p.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}