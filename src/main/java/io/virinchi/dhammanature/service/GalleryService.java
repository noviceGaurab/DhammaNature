package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Gallery;
import io.virinchi.dhammanature.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public List<Gallery> all() {
        return galleryRepository.findAll();
    }

    public Gallery add(String title, byte[] imageData, String imageContentType, String description) {
        return galleryRepository.save(Gallery.builder()
                .title(title).imageData(imageData).imageContentType(imageContentType).description(description)
                .imageUrl("").build());
    }

    /** Creates a bare gallery entry (no image) from the public REST API (POST /api/gallery). */
    public Gallery createFromApi(String title, String description) {
        return galleryRepository.save(Gallery.builder()
                .title(title).description(description).imageUrl("").build());
    }

    @Transactional
    public void delete(Integer id) {
        galleryRepository.deleteById(id);
    }
}
