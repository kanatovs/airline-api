package com.example.airlineapi.flight;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(Long id) {
        super("Flight " + id + " was not found");
    }
}
