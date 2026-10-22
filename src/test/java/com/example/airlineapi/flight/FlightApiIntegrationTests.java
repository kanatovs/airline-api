package com.example.airlineapi.flight;

import com.example.airlineapi.flight.repository.FlightRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.properties")
class FlightApiIntegrationTests {
    private static final String VALID_FLIGHT = """
            {"flightNumber":"KZ100","fromAirport":"ALA","toAirport":"NQZ",
             "departureTime":"2026-10-09T14:30:00"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FlightRepository repository;

    @BeforeEach
    void clearFlights() {
        repository.deleteAll();
    }

    @Test
    void completeCrudRoundTripUsesPersistentData() throws Exception {
        mvc.perform(get("/flights")).andExpect(status().isOk()).andExpect(content().json("[]"));

        String created = mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(VALID_FLIGHT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("KZ100"))
                .andExpect(jsonPath("$.fromAirport").value("ALA"))
                .andExpect(jsonPath("$.toAirport").value("NQZ"))
                .andExpect(jsonPath("$.departureTime").value("2026-10-09T14:30:00"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(created, "$.id");

        mvc.perform(get("/flights/{id}", id)).andExpect(status().isOk()).andExpect(content().json(created));
        mvc.perform(get("/flights")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(id.intValue()));

        mvc.perform(put("/flights/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_FLIGHT.replace("KZ100", "KZ101")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.intValue()))
                .andExpect(jsonPath("$.flightNumber").value("KZ101"));
        mvc.perform(get("/flights/{id}", id)).andExpect(jsonPath("$.flightNumber").value("KZ101"));

        mvc.perform(delete("/flights/{id}", id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/flights/{id}", id)).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void allOperationsOnMissingFlightReturn404() throws Exception {
        mvc.perform(get("/flights/999999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Flight not found"));
        mvc.perform(put("/flights/999999").contentType(MediaType.APPLICATION_JSON).content(VALID_FLIGHT))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/flights/999999")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"flightNumber\":\" \"}",
            "{\"flightNumber\":\"ABCDEFGHIJK\",\"fromAirport\":\"ALA\",\"toAirport\":\"NQZ\",\"departureTime\":\"2026-10-09T14:30:00\"}",
            "{\"flightNumber\":\"<script>\",\"fromAirport\":\"ALA\",\"toAirport\":\"NQZ\",\"departureTime\":\"2026-10-09T14:30:00\"}",
            "{\"flightNumber\":\"KZ100\",\"fromAirport\":\"Almaty\",\"toAirport\":\"NQZ\",\"departureTime\":\"2026-10-09T14:30:00\"}",
            "{\"flightNumber\":\"KZ100\",\"fromAirport\":\"ALA\",\"toAirport\":\"12\",\"departureTime\":\"2026-10-09T14:30:00\"}",
            "{\"flightNumber\":\"KZ100\",\"fromAirport\":\"ALA\",\"toAirport\":\"ALA\",\"departureTime\":\"2026-10-09T14:30:00\"}",
            "{\"flightNumber\":\"KZ100\",\"fromAirport\":\"ALA\",\"toAirport\":\"NQZ\",\"departureTime\":null}"
    })
    void invalidCreateDoesNotWriteToDatabase(String body) throws Exception {
        mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isMap());
        assertThat(repository.count()).isZero();
    }

    @Test
    void invalidUpdateDoesNotChangeExistingFlight() throws Exception {
        String created = mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(VALID_FLIGHT))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(created, "$.id");
        mvc.perform(put("/flights/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/flights/{id}", id)).andExpect(content().json(created));
    }

    @Test
    void whitespaceAndLowercaseCodesAreNormalized() throws Exception {
        mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_FLIGHT.replace("KZ100", " kz100 ").replace("ALA", " ala ")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.flightNumber").value("KZ100"))
                .andExpect(jsonPath("$.fromAirport").value("ALA"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not JSON", "{", "{\"departureTime\":\"not a date\"}"})
    void malformedBodyReturns400(String body) throws Exception {
        mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        assertThat(repository.count()).isZero();
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mvc.perform(get("/flights/not-a-number")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Flight id must be an integer"));
    }

    @Test
    void browserInterfaceIsServedFromTheApiOrigin() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"flight-form\"")));
    }
}
