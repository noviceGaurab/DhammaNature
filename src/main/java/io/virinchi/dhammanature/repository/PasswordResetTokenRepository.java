package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Marks all outstanding reset tokens for a user as used, so only the newest one stays valid. */
    @Modifying(clearAutomatically = true)
    @Query("update PasswordResetToken t set t.used = true where t.user.id = :userId and t.used = false")
    int invalidateTokensFor(@Param("userId") Integer userId);
}