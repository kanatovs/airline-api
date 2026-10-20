package com.example.airlineapi.flight;

import com.example.airlineapi.flight.domain.Flight;
import com.example.airlineapi.flight.repository.FlightRepository;
import com.example.airlineapi.flight.service.FlightServiceImpl;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@Import(FlightServiceImpl.class)
class FlightControllerTests {

    private static final String FLIGHT_JSON = """
            {
              "flightNumber": "KC102",
              "fromAirport": "ALA",
              "toAirport": "TAS",
              "departureTime": "2026-11-20T10:00:00"
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FlightRepository repository;

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void missingFlightReturnsSafeNotFoundProblem(String method) throws Exception {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        mvc.perform(flightRequest(method, "/flights/42"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Flight not found"))
                .andExpect(jsonPath("$.instance").value("/flights/42"));

        verify(repository).findById(42L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void existingFlightCanBeRead() throws Exception {
        when(repository.findById(42L)).thenReturn(Optional.of(flight("KC101")));

        mvc.perform(get("/flights/42"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flightNumber").value("KC101"))
                .andExpect(jsonPath("$.fromAirport").value("ALA"))
                .andExpect(jsonPath("$.toAirport").value("TAS"))
                .andExpect(jsonPath("$.departureTime").value("2026-11-20T10:00:00"));
    }

    @Test
    void existingFlightCanBeUpdated() throws Exception {
        Flight existing = flight("KC101");
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        mvc.perform(put("/flights/42").contentType(MediaType.APPLICATION_JSON).content(FLIGHT_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("KC102"))
                .andExpect(jsonPath("$.fromAirport").value("ALA"))
                .andExpect(jsonPath("$.toAirport").value("TAS"))
                .andExpect(jsonPath("$.departureTime").value("2026-11-20T10:00:00"));

        verify(repository).save(existing);
    }

    @Test
    void existingFlightCanBeDeleted() throws Exception {
        Flight existing = flight("KC101");
        when(repository.findById(42L)).thenReturn(Optional.of(existing));

        mvc.perform(delete("/flights/42"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(repository).delete(existing);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void malformedIdRemainsBadRequest(String method) throws Exception {
        mvc.perform(flightRequest(method, "/flights/not-a-number"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void unexpectedRepositoryFailureIsNotHandledAsNotFound(String method) {
        RuntimeException failure = new IllegalStateException("Database unavailable");
        when(repository.findById(42L)).thenThrow(failure);

        ServletException thrown = catchThrowableOfType(
                ServletException.class, () -> mvc.perform(flightRequest(method, "/flights/42")));

        assertThat(thrown).isNotNull();
        assertThat(thrown.getCause()).isSameAs(failure);
        verify(repository).findById(42L);
        verifyNoMoreInteractions(repository);
    }

    private MockHttpServletRequestBuilder flightRequest(String method, String path) {
        MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(method), path);
        if (method.equals("PUT")) {
            request.contentType(MediaType.APPLICATION_JSON).content(FLIGHT_JSON);
        }
        return request;
    }

    private Flight flight(String number) {
        return new Flight(number, "ALA", "TAS", LocalDateTime.of(2026, 11, 20, 10, 0));
    }
}
