package io.virinchi.dhammanature.config;

import io.virinchi.dhammanature.model.SiteAsset;
import io.virinchi.dhammanature.repository.SiteAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time migration: reads every file under classpath:static/assets/images/ and
 * stores it as a SiteAsset BLOB row keyed by the filename without its extension
 * (e.g. "logo.png" becomes name = "logo"). Already-existing names are skipped so the
 * runner is safe to run on every startup. After the templates are switched to the
 * /site-assets/{name}/image endpoint the physical files can be deleted.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SiteAssetBackfillRunner implements CommandLineRunner {

    private static final String ASSETS_LOCATION = "classpath*:static/assets/images/*";

    private final SiteAssetRepository siteAssetRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ASSETS_LOCATION);
        if (resources.length == 0) {
            log.info("SiteAsset backfill: no files found in " + ASSETS_LOCATION + " - nothing to do.");
            return;
        }

        int inserted = 0;
        int skipped = 0;
        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null || fileName.isEmpty() || notAnAsset(fileName)) {
                log.debug("SiteAsset backfill: ignoring {}", fileName);
                continue;
            }
            String name = nameWithoutExtension(fileName);
            if (siteAssetRepository.findByName(name).isPresent()) {
                skipped++;
                log.info("SiteAsset backfill: {} already exists - skipped", name);
                continue;
            }
            try (var in = resource.getInputStream()) {
                SiteAsset asset = SiteAsset.builder()
                        .name(name)
                        .contentType(contentTypeFor(fileName))
                        .imageData(in.readAllBytes())
                        .build();
                siteAssetRepository.save(asset);
                inserted++;
                log.info("SiteAsset backfill: stored {} ({} bytes, {})", name, asset.getImageData().length, asset.getContentType());
            }
        }
        log.info("SiteAsset backfill finished: inserted={}, skipped={}", inserted, skipped);
    }

    private static boolean notAnAsset(String fileName) {
        String lower = fileName.toLowerCase();
        return !lower.endsWith(".png") && !lower.endsWith(".jpg")
                && !lower.endsWith(".jpeg") && !lower.endsWith(".gif")
                && !lower.endsWith(".webp") && !lower.endsWith(".svg");
    }

    private static String nameWithoutExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String contentTypeFor(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}