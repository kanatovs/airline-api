package com.example.airlineapi.flight.domain;

import com.example.airlineapi.common.Printable;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "flights")
public class Flight implements Printable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String flightNumber;

    @Column(nullable = false, length = 3)
    private String fromAirport;

    @Column(nullable = false, length = 3)
    private String toAirport;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    protected Flight() {
    }

    public Flight(String flightNumber, String fromAirport, String toAirport, LocalDateTime departureTime) {
        this.flightNumber = flightNumber;
        this.fromAirport = fromAirport;
        this.toAirport = toAirport;
        this.departureTime = departureTime;
    }

    public Long getId() { return id; }
    public String getFlightNumber() { return flightNumber; }
    public String getFromAirport() { return fromAirport; }
    public String getToAirport() { return toAirport; }
    public LocalDateTime getDepartureTime() { return departureTime; }

    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public void setFromAirport(String fromAirport) { this.fromAirport = fromAirport; }
    public void setToAirport(String toAirport) { this.toAirport = toAirport; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    @Override
    public void print() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                ", flightNumber='" + flightNumber + '\'' +
                ", fromAirport='" + fromAirport + '\'' +
                ", toAirport='" + toAirport + '\'' +
                ", departureTime=" + departureTime +
                '}';
    }

    // Обычно identity для Entity — по id, если он есть
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight flight)) return false;
        return id != null && Objects.equals(id, flight.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String flightNumber;
        private String fromAirport;
        private String toAirport;
        private LocalDateTime departureTime;

        public Builder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public Builder fromAirport(String fromAirport) {
            this.fromAirport = fromAirport;
            return this;
        }

        public Builder toAirport(String toAirport) {
            this.toAirport = toAirport;
            return this;
        }

        public Builder departureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Flight build() {
            return new Flight(flightNumber, fromAirport, toAirport, departureTime);
        }
    }
}
