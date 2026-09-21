package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Gallery;
import io.virinchi.dhammanature.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public List<Gallery> all() {
        return galleryRepository.findAll();
    }

    public Page<Gallery> pagedAll(Pageable pageable) {
        return galleryRepository.findAll(pageable);
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

    public Gallery get(Integer id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Gallery image not found"));
    }

    /** Admin edit of a gallery image's title/description, with an optional replacement image. */
    @Transactional
    public Gallery update(Integer id, String title, String description, byte[] newImage, String newContentType) {
        Gallery g = get(id);
        g.setTitle(title.trim());
        g.setDescription(description);
        if (newImage != null && newImage.length > 0) {
            g.setImageData(newImage);
            g.setImageContentType(newContentType);
        }
        return galleryRepository.save(g);
    }
}
