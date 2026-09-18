package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gallery")
public class GalleryImageController {

    private final GalleryRepository galleryRepository;

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getGalleryImage(@PathVariable Integer id) {
        return galleryRepository.findById(id)
                .filter(g -> g.getImageData() != null)
                .map(g -> ResponseEntity.ok()
                        .contentType(g.getImageContentType() != null
                                ? MediaType.parseMediaType(g.getImageContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(g.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}