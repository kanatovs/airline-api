package com.example.airlineapi.flight.dto;

import com.example.airlineapi.flight.domain.Flight;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Locale;

public record FlightRequest(
        @NotBlank(message = "Flight number is required")
        @Size(max = 10, message = "Flight number must contain at most 10 characters")
        @Pattern(regexp = "[A-Z0-9-]+", message = "Flight number must contain letters, digits or hyphens")
        String flightNumber,
        @NotBlank(message = "Departure airport is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Departure airport must be a three-letter IATA code")
        String fromAirport,
        @NotBlank(message = "Arrival airport is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Arrival airport must be a three-letter IATA code")
        String toAirport,
        @NotNull(message = "Departure time is required") LocalDateTime departureTime
) {
    public FlightRequest {
        flightNumber = normalize(flightNumber);
        fromAirport = normalize(fromAirport);
        toAirport = normalize(toAirport);
    }

    @AssertTrue(message = "Departure and arrival airports must be different")
    public boolean isRouteDifferent() {
        return fromAirport == null || toAirport == null || !fromAirport.equals(toAirport);
    }

    public Flight toFlight() {
        return new Flight(flightNumber, fromAirport, toAirport, departureTime);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
