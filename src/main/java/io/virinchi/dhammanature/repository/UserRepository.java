package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRole(io.virinchi.dhammanature.model.enums.Role role);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.wishlist w LEFT JOIN FETCH w.vendor WHERE u.id = :id")
    Optional<User> findByIdWithWishlist(@Param("id") Integer id);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.followedCenters WHERE u.id = :id")
    Optional<User> findByIdWithFollowedCenters(@Param("id") Integer id);

    /** Cheap "heartbeat" update used to keep the AFK / "the community misses you" nudges accurate. */
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.lastSeenAt = :now where u.id = :id and (u.lastSeenAt is null or u.lastSeenAt < :since)")
    int touchLastSeen(@Param("id") Integer id, @Param("now") LocalDateTime now, @Param("since") LocalDateTime since);
}
