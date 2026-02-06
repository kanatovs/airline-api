package com.example.airlineapi.flight.service;

import com.example.airlineapi.flight.domain.Flight;

import java.util.List;

public interface FlightService {
    List<Flight> getAll();
    Flight getById(Long id);

    Flight create(Flight flight);
    Flight update(Long id, Flight flight);
    void delete(Long id);
}
