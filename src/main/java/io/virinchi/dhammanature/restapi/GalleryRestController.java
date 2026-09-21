package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.dto.GalleryCreateRequest;
import io.virinchi.dhammanature.model.Gallery;
import io.virinchi.dhammanature.repository.GalleryRepository;
import io.virinchi.dhammanature.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gallery")
public class GalleryRestController {

    private final GalleryRepository galleryRepository;
    private final GalleryService galleryService;

    @GetMapping
    public ResponseEntity<?> getAllGalleryItems() {
        return ResponseEntity.ok(galleryRepository.findAll().stream().map(ApiViews::gallery).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGalleryById(@PathVariable("id") Integer id) {
        return galleryRepository.findById(id)
                .map(gallery -> ResponseEntity.ok((Object) ApiViews.gallery(gallery)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(error("Gallery item " + id + " not found")));
    }

    @PostMapping
    public ResponseEntity<?> saveGallery(@RequestBody GalleryCreateRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(error("A title is required."));
        }
        try {
            Gallery saved = galleryService.createFromApi(request.title().trim(), request.description());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.gallery(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(error("Could not save gallery item - invalid data."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return body;
    }
}