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
                .load();

        try {
            flyway.migrate();
            log.info("Flyway migration completed successfully (dev initializer).");
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            log.warn("Flyway migrate failed: {}", errorMessage);

            // Check if baseline is already present
            if (errorMessage != null && errorMessage.contains("already contains migrations")) {
                log.info("Flyway schema history already exists. Skipping baseline. Use 'flyway repair' if needed.");
                return;
            }

            // Try baseline only if schema history doesn't exist or is empty
            try {
                flyway.baseline();
                flyway.migrate();
                log.info("Flyway baseline + migrate succeeded (dev initializer).");
            } catch (Exception ex2) {
                String baselineError = ex2.getMessage();
                if (baselineError != null && baselineError.contains("already contains migrations")) {
                    log.info("Flyway schema history already contains migrations. Application will continue.");
                } else {
                    log.error("Flyway baseline/migrate failed in dev initializer: {}", baselineError);
                }
            }
        }
    }
}
