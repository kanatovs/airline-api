package com.example.airlineapi.flight.repository;

import com.example.airlineapi.flight.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {
}
