package org.qurium;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;

@ApplicationScoped
public class ApplicationInit {

    /*@ConfigProperty(name = "quarkus.datasource.jdbc.url")
    private String url;

    @ConfigProperty(name = "quarkus.datasource.username")
    private String username;

    @ConfigProperty(name = "quarkus.datasource.password")
    private String password;

    @Startup
    void start() {
        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        System.out.println("Database migration completed successfully!");
    }*/
}
