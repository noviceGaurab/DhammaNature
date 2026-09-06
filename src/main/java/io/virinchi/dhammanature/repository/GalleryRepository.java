package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GalleryRepository extends JpaRepository<Gallery, Integer> {
    List<Gallery> findByMeditationCenter_Id(Integer centerId);
}
