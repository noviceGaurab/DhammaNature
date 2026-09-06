package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.MeditationCenter;
import io.virinchi.dhammanature.repository.MeditationCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/** FR-03: Meditation Center Directory + NFR-03: Organization Profile. */
@Service
@RequiredArgsConstructor
public class MeditationCenterService {

    private final MeditationCenterRepository meditationCenterRepository;

    public List<MeditationCenter> directory() {
        return meditationCenterRepository.findByVerifiedTrue();
    }

    public List<MeditationCenter> all() {
        return meditationCenterRepository.findAll();
    }

    public MeditationCenter get(Integer id) {
        return meditationCenterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Meditation center not found"));
    }

    public MeditationCenter save(MeditationCenter center) {
        return meditationCenterRepository.save(center);
    }

    public List<MeditationCenter> search(String query) {
        return meditationCenterRepository.findByNameContainingIgnoreCase(query == null ? "" : query);
    }
}
