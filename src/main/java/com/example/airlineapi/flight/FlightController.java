package com.example.airlineapi.flight;

import com.example.airlineapi.flight.domain.Flight;
import com.example.airlineapi.flight.service.FlightService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService service;

    public FlightController(FlightService service) {
        this.service = service;
    }

    @GetMapping
    public List<Flight> all() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Flight one(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flight create(@RequestBody Flight flight) {
        return service.create(flight);
    }

    @PutMapping("/{id}")
    public Flight update(@PathVariable Long id, @RequestBody Flight flight) {
        return service.update(id, flight);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
