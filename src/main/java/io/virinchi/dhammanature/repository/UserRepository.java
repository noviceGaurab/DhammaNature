package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRole(io.virinchi.dhammanature.model.enums.Role role);
}
