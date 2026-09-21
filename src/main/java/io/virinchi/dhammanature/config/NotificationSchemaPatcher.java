package io.virinchi.dhammanature.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Data-only fix for a schema drift: the notification.type column was created by
 * Hibernate as VARCHAR(9) (longest enum at the time: "VOLUNTEER"). Newer enum
 * values - USER_REPORT (11), DHAMMA_QUOTE (12), PRIVATE_MESSAGE (14) - are then
 * truncated on INSERT, surfacing as "Data truncated for column 'type'". Widening
 * the column on startup keeps the live TiDB schema in sync. The entity now also
 * declares length = 32 so freshly created databases never hit this.
 */
@Component
@RequiredArgsConstructor
public class NotificationSchemaPatcher implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Integer charLength = jdbcTemplate.queryForObject(
                    "SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notification' AND COLUMN_NAME = 'type'",
                    Integer.class);
            if (charLength == null || charLength < 32) {
                jdbcTemplate.execute("ALTER TABLE notification MODIFY COLUMN type VARCHAR(32) NOT NULL");
            }
        } catch (Exception ignored) {
            // Table/column may not exist yet, or the DB hides information_schema - never block startup.
        }
    }
}