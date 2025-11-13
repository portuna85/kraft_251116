package com.kraft.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Profile("dev")
public class FlywayDevInitializer {

    private static final Logger log = LoggerFactory.getLogger(FlywayDevInitializer.class);

    private final DataSource dataSource;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    @Value("${spring.flyway.baseline-on-migrate:true}")
    private boolean baselineOnMigrate;

    public FlywayDevInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrateOrBaseline() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations.split(","))
                .baselineOnMigrate(baselineOnMigrate)
                .outOfOrder(false)
                .load();

        try {
            flyway.migrate();
            log.info("Flyway migration completed successfully.");
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();

            // Validation failed - migration exists in code but not in DB history
            if (errorMessage != null && errorMessage.contains("Validate failed")) {
                log.info("Flyway detected schema/history mismatch. Attempting repair + migrate...");
                try {
                    flyway.repair();
                    flyway.migrate();
                    log.info("Flyway repair + migrate succeeded.");
                } catch (Exception repairEx) {
                    log.warn("Flyway repair failed. Schema and history may be out of sync. Manual intervention may be required.");
                }
                return;
            }

            // Baseline already exists with migrations
            if (errorMessage != null && errorMessage.contains("already contains migrations")) {
                log.info("Flyway schema history already exists. Skipping baseline.");
                return;
            }

            // Unknown error - try baseline as fallback
            try {
                flyway.baseline();
                flyway.migrate();
                log.info("Flyway baseline + migrate succeeded.");
            } catch (Exception ex2) {
                log.error("Flyway migration failed: {}", ex2.getMessage());
            }
        }
    }
}
