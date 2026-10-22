package com.example.airlineapi.flight;

import com.example.airlineapi.flight.dto.FlightRequest;
import com.example.airlineapi.flight.dto.FlightResponse;
import com.example.airlineapi.flight.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService service;

    public FlightController(FlightService service) {
        this.service = service;
    }

    @GetMapping
    public List<FlightResponse> all() {
        return service.getAll().stream().map(FlightResponse::from).toList();
    }

    @GetMapping("/{id}")
    public FlightResponse one(@PathVariable Long id) {
        return FlightResponse.from(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlightResponse create(@Valid @RequestBody FlightRequest request) {
        return FlightResponse.from(service.create(request.toFlight()));
    }

    @PutMapping("/{id}")
    public FlightResponse update(@PathVariable Long id, @Valid @RequestBody FlightRequest request) {
        return FlightResponse.from(service.update(id, request.toFlight()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
