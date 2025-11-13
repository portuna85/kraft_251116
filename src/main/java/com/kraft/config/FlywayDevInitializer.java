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
            log.warn("Flyway migrate failed, attempting baseline + migrate: {}", ex.getMessage());
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
