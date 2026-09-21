package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves a user's profile photo from the BLOB column stored in the database. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserImageController {

    private final UserRepository userRepository;

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Integer id) {
        return userRepository.findById(id)
                .filter(u -> u.getProfileImageData() != null)
                .map(u -> ResponseEntity.ok()
                        .contentType(u.getProfileImageContentType() != null
                                ? MediaType.parseMediaType(u.getProfileImageContentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(u.getProfileImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
