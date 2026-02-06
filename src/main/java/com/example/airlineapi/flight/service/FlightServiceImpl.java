package com.example.airlineapi.flight.service;

import com.example.airlineapi.flight.domain.Flight;
import com.example.airlineapi.flight.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightServiceImpl implements FlightService {

    private final FlightRepository repository;

    public FlightServiceImpl(FlightRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Flight> getAll() {
        return repository.findAll();
    }

    @Override
    public Flight getById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public Flight create(Flight flight) {
        return repository.save(flight);
    }

    @Override
    public Flight update(Long id, Flight flight) {
        Flight existing = getById(id);

        existing.setFlightNumber(flight.getFlightNumber());
        existing.setFromAirport(flight.getFromAirport());
        existing.setToAirport(flight.getToAirport());
        existing.setDepartureTime(flight.getDepartureTime());

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
