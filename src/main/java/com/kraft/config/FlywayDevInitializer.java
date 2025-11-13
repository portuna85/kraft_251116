package com.kraft.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

@Configuration
@Profile("dev")
public class FlywayDevInitializer {

    private static final Logger log = LoggerFactory.getLogger(FlywayDevInitializer.class);

    private final Flyway flyway;

    public FlywayDevInitializer(Flyway flyway) {
        this.flyway = flyway;
    }

    @PostConstruct
    public void migrateOrBaseline() {
        try {
            flyway.migrate();
            log.info("Flyway migration completed successfully (dev initializer).");
        } catch (Exception ex) {
            log.warn("Flyway migrate failed, attempting baseline-on-migrate: {}", ex.getMessage());
            try {
                flyway.baseline();
                flyway.migrate();
                log.info("Flyway baseline + migrate succeeded (dev initializer).");
            } catch (Exception ex2) {
                log.error("Flyway baseline/migrate failed in dev initializer: {}", ex2.getMessage());
            }
        }
    }
}
