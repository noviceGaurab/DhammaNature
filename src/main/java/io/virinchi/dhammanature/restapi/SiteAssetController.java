package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.SiteAsset;
import io.virinchi.dhammanature.repository.SiteAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/site-assets")
public class SiteAssetController {

    private final SiteAssetRepository siteAssetRepository;

    @GetMapping("/{name}/image")
    public ResponseEntity<byte[]> getSiteAssetImage(@PathVariable String name) {
        Optional<SiteAsset> asset = siteAssetRepository.findByName(name);
        if (asset.isEmpty()) {
            asset = siteAssetRepository.findByName(name.replaceAll("\\.(jpg|jpeg|png|gif|webp|svg)$", ""));
        }
        return asset.filter(a -> a.getImageData() != null)
                .map(a -> ResponseEntity.ok()
                        .contentType(a.getContentType() != null
                                ? MediaType.parseMediaType(a.getContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(a.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}