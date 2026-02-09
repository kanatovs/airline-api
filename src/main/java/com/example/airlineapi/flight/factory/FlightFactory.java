package com.example.airlineapi.flight.factory;

import com.example.airlineapi.flight.domain.Flight;

import java.time.LocalDateTime;

public final class FlightFactory {

    private FlightFactory() {}

    public static Flight createDefaultFlight() {
        // Builder используется внутри Factory
        return Flight.builder()
                .flightNumber("KZ100")
                .fromAirport("ALA")
                .toAirport("NQZ")
                .departureTime(LocalDateTime.now().plusDays(1))
                .build();
    }
}
