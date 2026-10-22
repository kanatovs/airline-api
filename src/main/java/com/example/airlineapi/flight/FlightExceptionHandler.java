package com.example.airlineapi.flight;

import com.example.airlineapi.flight.domain.FlightNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FlightExceptionHandler {

    @ExceptionHandler(FlightNotFoundException.class)
    public ProblemDetail handleFlightNotFound(FlightNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
