package com.kraft.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Provides a safe Flyway migration strategy used by Spring Boot when a FlywayMigrationStrategy bean is present.
 * This strategy attempts migrate(); on validation failure it will attempt baseline() then migrate() again.
 * This prevents application startup failing due to existing schema without flyway history table.
 */
@Configuration
@Profile("!test")
public class FlywaySafeMigrationStrategy {

    private static final Logger log = LoggerFactory.getLogger(FlywaySafeMigrationStrategy.class);

    @Bean
    @ConditionalOnMissingBean
    public org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                flyway.migrate();
                log.info("Flyway migrate completed.");
            } catch (FlywayException ex) {
                String message = ex.getMessage();
                // Check if this is a validation failure
                if (message != null && (message.contains("Validate failed") ||
                                       message.contains("not applied to database") ||
                                       message.contains("validation"))) {
                    log.warn("Flyway validation failed: {}. Attempting baseline + migrate.", message);
                } else {
                    log.warn("Flyway migrate failed: {}. Attempting baseline + migrate.", message);
                }

                try {
                    flyway.baseline();
                    flyway.migrate();
                    log.info("Flyway baseline + migrate succeeded.");
                } catch (FlywayException ex2) {
                    log.error("Flyway baseline/migrate also failed: {}", ex2.getMessage());
                }
            } catch (Exception ex) {
                log.error("Unexpected error during Flyway migration: {}", ex.getMessage());
            }
        };
    }
}

