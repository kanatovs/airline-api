package com.example.airlineapi;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigurationTests {

    @Test
    void bindsProcessEnvironmentWithoutOpeningAConnection() {
        contextWithEnvironment(Map.of(
                "SPRING_DATASOURCE_URL", "jdbc:postgresql://127.0.0.1:15432/airline_config_test",
                "SPRING_DATASOURCE_USERNAME", "airline_config_test",
                "SPRING_DATASOURCE_PASSWORD", "unit-test-only-password"
        )).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(HikariDataSource.class);
            HikariDataSource dataSource = context.getBean(HikariDataSource.class);
            assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:postgresql://127.0.0.1:15432/airline_config_test");
            assertThat(dataSource.getUsername()).isEqualTo("airline_config_test");
            assertThat(dataSource.getPassword()).isEqualTo("unit-test-only-password");
            assertThat(dataSource.getHikariPoolMXBean()).isNull();
        });
    }

    @Test
    void hasNoDefaultDatabaseCredentials() {
        contextWithEnvironment(Map.of(
                "SPRING_DATASOURCE_URL", "jdbc:postgresql://127.0.0.1:15432/airline_config_test"
        )).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(HikariDataSource.class);
            HikariDataSource dataSource = context.getBean(HikariDataSource.class);
            assertThat(dataSource.getUsername()).isNull();
            assertThat(dataSource.getPassword()).isNull();
            assertThat(dataSource.getHikariPoolMXBean()).isNull();
        });
    }

    @Test
    void missingDatabaseUrlDoesNotFallBackToACheckedInDatabase() {
        contextWithEnvironment(Map.of()).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("Failed to determine a suitable driver class");
        });
    }

    private ApplicationContextRunner contextWithEnvironment(Map<String, String> variables) {
        return new ApplicationContextRunner()
                .withPropertyValues("spring.config.location=classpath:/application.properties")
                .withInitializer(context -> {
                    var sources = context.getEnvironment().getPropertySources();
                    sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                    sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                    sources.addFirst(new SystemEnvironmentPropertySource(
                            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                            new HashMap<>(variables)
                    ));
                })
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class));
    }
}
