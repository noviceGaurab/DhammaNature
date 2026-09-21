package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.dto.GalleryCreateRequest;
import io.virinchi.dhammanature.model.Gallery;
import io.virinchi.dhammanature.repository.GalleryRepository;
import io.virinchi.dhammanature.service.GalleryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gallery")
public class GalleryRestController {

    private final GalleryRepository galleryRepository;
    private final GalleryService galleryService;
    private final ApiAuth apiAuth;

    @GetMapping
    public ResponseEntity<?> getAllGalleryItems() {
        return ResponseEntity.ok(galleryRepository.findAll().stream().map(ApiViews::gallery).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGalleryById(@PathVariable("id") Integer id) {
        Gallery gallery = galleryRepository.findById(id).orElseThrow(
                () -> ApiException.notFound("Gallery item " + id + " not found"));
        return ResponseEntity.ok(ApiViews.gallery(gallery));
    }

    /** Admin only: adds a gallery item. */
    @PostMapping
    public ResponseEntity<?> saveGallery(@Valid @RequestBody GalleryCreateRequest request, HttpSession session) {
        apiAuth.requireAdmin(session);
        Gallery saved = galleryService.createFromApi(request.title().trim(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.gallery(saved));
    }
}
