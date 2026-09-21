package io.virinchi.dhammanature.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Self-skipping migration for the vendor approval status (PENDING / VERIFIED / REJECTED).
 *
 * <p>Older databases still contain the legacy {@code vendor.verified} column, which the entity no
 * longer maps. On such a database this patcher:
 * <ol>
 *   <li>copies the old flag into the new {@code status} column (Hibernate adds {@code status} as
 *       NOT NULL, so existing rows end up with an empty string, not NULL), and</li>
 *   <li>makes {@code verified} nullable so new vendor inserts, which no longer write that column,
 *       do not fail with "Field 'verified' doesn't have a default value".</li>
 * </ol>
 * On a brand-new database there is no {@code verified} column, so nothing happens and startup is
 * never blocked. It runs before {@link DataSeeder}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class VendorSchemaPatcher implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            String legacyType = legacyColumnType();
            if (legacyType == null) {
                return; // fresh database: no legacy column, nothing to migrate
            }
            jdbcTemplate.update("UPDATE vendor SET status = CASE WHEN verified = 1 THEN 'VERIFIED' "
                    + "ELSE 'PENDING' END WHERE status IS NULL OR status = ''");
            jdbcTemplate.execute("ALTER TABLE vendor MODIFY COLUMN verified " + legacyType + " NULL");
        } catch (Exception ignored) {
            // Never block startup: the table may not exist yet or information_schema may be hidden.
        }
    }

    /** Returns the SQL type of the legacy {@code vendor.verified} column, or null if it does not exist. */
    private String legacyColumnType() {
        List<String> types = jdbcTemplate.queryForList(
                "SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'vendor' AND COLUMN_NAME = 'verified'", String.class);
        return types.isEmpty() ? null : types.get(0);
    }
}