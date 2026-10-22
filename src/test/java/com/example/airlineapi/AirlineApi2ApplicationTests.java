package com.example.airlineapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.properties")
class AirlineApi2ApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        String requestedUrl = System.getenv("TEST_DATABASE_URL");
        String expectedUrl = "jdbc:h2:mem:airline-test";
        if (requestedUrl != null) {
            assertThat(requestedUrl).isNotBlank();
            expectedUrl = requestedUrl.split(";", 2)[0];
        }
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith(expectedUrl);
        }
    }

}
