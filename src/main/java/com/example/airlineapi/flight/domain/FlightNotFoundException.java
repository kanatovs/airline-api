package com.example.airlineapi.flight.domain;

public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException() {
        super("Flight not found");
    }
}
