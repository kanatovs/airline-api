package com.example.airlineapi.flight.dto;

import com.example.airlineapi.flight.domain.Flight;

import java.time.LocalDateTime;

public record FlightResponse(Long id, String flightNumber, String fromAirport,
                             String toAirport, LocalDateTime departureTime) {
    public static FlightResponse from(Flight flight) {
        return new FlightResponse(flight.getId(), flight.getFlightNumber(), flight.getFromAirport(),
                flight.getToAirport(), flight.getDepartureTime());
    }
}
