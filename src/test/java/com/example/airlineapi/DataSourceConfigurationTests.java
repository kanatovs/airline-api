package com.example.airlineapi;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigurationTests {

    private static final Map<String, String> APPLICATION_DATABASE_ENVIRONMENT = Map.of(
            "SPRING_DATASOURCE_URL", "jdbc:postgresql://127.0.0.1:15432/application_database_do_not_use",
            "SPRING_DATASOURCE_DRIVER_CLASS_NAME", "org.postgresql.Driver",
            "SPRING_DATASOURCE_USERNAME", "application_role_do_not_use",
            "SPRING_DATASOURCE_PASSWORD", "application-test-only-password"
    );

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

    @Test
    void testDefaultsIgnoreApplicationDatabaseEnvironmentWithoutOpeningAConnection() {
        testContextWithEnvironment(APPLICATION_DATABASE_ENVIRONMENT).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(HikariDataSource.class);
            HikariDataSource dataSource = context.getBean(HikariDataSource.class);
            assertThat(dataSource.getJdbcUrl()).isEqualTo(
                    "jdbc:h2:mem:airline-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
            assertThat(dataSource.getDriverClassName()).isEqualTo("org.h2.Driver");
            assertThat(dataSource.getUsername()).isEqualTo("sa");
            assertThat(dataSource.getPassword()).isEmpty();
            assertThat(dataSource.getHikariPoolMXBean()).isNull();
        });
    }

    @Test
    void explicitTestDatabaseOverridesApplicationEnvironmentWithoutOpeningAConnection() {
        Map<String, String> variables = new HashMap<>(APPLICATION_DATABASE_ENVIRONMENT);
        variables.putAll(Map.of(
                "TEST_DATABASE_URL", "jdbc:postgresql://127.0.0.1:15433/isolated_test_database",
                "TEST_DATABASE_DRIVER", "org.postgresql.Driver",
                "TEST_DATABASE_USERNAME", "isolated_test_role",
                "TEST_DATABASE_PASSWORD", "isolated-test-only-password"
        ));
        testContextWithEnvironment(variables).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(HikariDataSource.class);
            HikariDataSource dataSource = context.getBean(HikariDataSource.class);
            assertThat(dataSource.getJdbcUrl())
                    .isEqualTo("jdbc:postgresql://127.0.0.1:15433/isolated_test_database");
            assertThat(dataSource.getDriverClassName()).isEqualTo("org.postgresql.Driver");
            assertThat(dataSource.getUsername()).isEqualTo("isolated_test_role");
            assertThat(dataSource.getPassword()).isEqualTo("isolated-test-only-password");
            assertThat(dataSource.getHikariPoolMXBean()).isNull();
        });
    }

    private ApplicationContextRunner contextWithEnvironment(Map<String, String> variables) {
        return baseContextWithEnvironment(variables)
                .withClassLoader(new FilteredClassLoader("org.h2"));
    }

    private ApplicationContextRunner testContextWithEnvironment(Map<String, String> variables) {
        return baseContextWithEnvironment(variables)
                .withInitializer(context -> TestPropertySourceUtils.addPropertiesFilesToEnvironment(
                        context, "classpath:application-test.properties"));
    }

    private ApplicationContextRunner baseContextWithEnvironment(Map<String, String> variables) {
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
