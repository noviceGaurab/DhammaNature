package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Gallery;
import io.virinchi.dhammanature.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public List<Gallery> all() {
        return galleryRepository.findAll();
    }

    public Gallery add(String title, String imageUrl, String description) {
        return galleryRepository.save(Gallery.builder()
                .title(title).imageUrl(imageUrl).description(description).build());
    }
}
