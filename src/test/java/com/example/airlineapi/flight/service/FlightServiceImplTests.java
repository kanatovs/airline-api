package com.example.airlineapi.flight.service;

import com.example.airlineapi.flight.domain.Flight;
import com.example.airlineapi.flight.domain.FlightNotFoundException;
import com.example.airlineapi.flight.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FlightServiceImplTests {

    private FlightRepository repository;
    private FlightServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(FlightRepository.class);
        service = new FlightServiceImpl(repository);
    }

    @Test
    void missingFlightCannotBeRead() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(42L))
                .isInstanceOf(FlightNotFoundException.class)
                .hasMessage("Flight not found");

        verify(repository).findById(42L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void missingFlightCannotBeUpdatedOrInserted() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, flight("KC102")))
                .isInstanceOf(FlightNotFoundException.class);

        verify(repository).findById(42L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void missingFlightCannotBeDeleted() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(FlightNotFoundException.class);

        verify(repository).findById(42L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void readsExistingFlight() {
        Flight existing = flight("KC101");
        when(repository.findById(42L)).thenReturn(Optional.of(existing));

        assertThat(service.getById(42L)).isSameAs(existing);
    }

    @Test
    void updatesTheExistingFlightWithAllRequestedFields() {
        Flight existing = flight("KC101");
        Flight replacement = new Flight("HY202", "TAS", "NQZ",
                LocalDateTime.of(2026, 11, 21, 12, 30));
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.update(42L, replacement)).isSameAs(existing);

        assertThat(existing.getFlightNumber()).isEqualTo(replacement.getFlightNumber());
        assertThat(existing.getFromAirport()).isEqualTo(replacement.getFromAirport());
        assertThat(existing.getToAirport()).isEqualTo(replacement.getToAirport());
        assertThat(existing.getDepartureTime()).isEqualTo(replacement.getDepartureTime());
        verify(repository).save(existing);
    }

    @Test
    void deletesTheExistingFlight() {
        Flight existing = flight("KC101");
        when(repository.findById(42L)).thenReturn(Optional.of(existing));

        service.delete(42L);

        verify(repository).delete(existing);
    }

    @Test
    void lookupFailureIsNotTranslatedToFlightNotFound() {
        RuntimeException failure = new IllegalStateException("Database unavailable");
        when(repository.findById(42L)).thenThrow(failure);

        assertThatThrownBy(() -> service.getById(42L)).isSameAs(failure);
        assertThatThrownBy(() -> service.update(42L, flight("KC102"))).isSameAs(failure);
        assertThatThrownBy(() -> service.delete(42L)).isSameAs(failure);
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(Flight.class));
    }

    @Test
    void saveFailureIsNotTranslatedToFlightNotFound() {
        Flight existing = flight("KC101");
        RuntimeException failure = new IllegalStateException("Database unavailable");
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenThrow(failure);

        assertThatThrownBy(() -> service.update(42L, flight("KC102"))).isSameAs(failure);
    }

    @Test
    void deleteFailureIsNotTranslatedToFlightNotFound() {
        Flight existing = flight("KC101");
        RuntimeException failure = new IllegalStateException("Database unavailable");
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        doThrow(failure).when(repository).delete(existing);

        assertThatThrownBy(() -> service.delete(42L)).isSameAs(failure);
    }

    private Flight flight(String number) {
        return new Flight(number, "ALA", "TAS", LocalDateTime.of(2026, 11, 20, 10, 0));
    }
}
