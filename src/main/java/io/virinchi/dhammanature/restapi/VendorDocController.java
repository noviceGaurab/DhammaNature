package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.VendorDocument;
import io.virinchi.dhammanature.repository.VendorDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves vendor application evidence (licence images, tax PDFs, shop proof...) to the admin review screen. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/vendor-docs")
public class VendorDocController {

    private final VendorDocumentRepository vendorDocumentRepository;

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> file(@PathVariable Integer id) {
        return vendorDocumentRepository.findById(id)
                .filter(d -> d.getData() != null)
                .map(d -> ResponseEntity.ok()
                        .contentType(d.getContentType() != null
                                ? MediaType.parseMediaType(d.getContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "inline; filename=\"" + safeName(d.getOriginalName()) + "\"")
                        .body(d.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

    private String safeName(String name) {
        return name == null || name.isBlank() ? "document" : name.replaceAll("[^A-Za-z0-9._ -]", "_");
    }
}