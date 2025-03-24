package controller;

import domain.Flight;
import domain.Seat;
import service.FlightService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    public void createFlight(String airlineId, String planeId, String origin, String destination, String departureDate, String departureTime, String arrivalDate, String arrivalTime, String price) {
        flightService.createFlight(airlineId, planeId, origin, destination, departureDate, departureTime, arrivalDate, arrivalTime, price);
    }

    public void getAllFlights() {
        List<Flight> flights = flightService.getAllFlights();
        for (Flight flight : flights) {
            System.out.println(flight.getFlightId() + ") " + flight.getOrigin().getAirportCode() + " - " + flight.getDestination().getAirportCode() + ": " + flight.getDeparture());
        }
    }

    public void getFilteredFlights(String origin, String destination, String date, String airline, String sortBy) {
        List<Flight> flights = flightService.getFilteredFlights(origin, destination, date, airline, sortBy);

        for (Flight flight : flights) {
            System.out.println(flight.getFlightId() + ") " + flight.getOrigin().getAirportCode() + " - " + flight.getDestination().getAirportCode() + ": " + flight.getDeparture());
        }
    }

    public void getAvailableSeats(String flightId) {
        Flight flight = flightService.getFlight(flightId);

        for (Seat seat : flight.getAvailableSeats()) {
            System.out.println(seat.getSeatId() + ") " + seat.getSeatNr() + ": " + seat.getSeatType() + " - " + seat.getSeatType().getPrice());
        }
    }

    public void updateFlight(String flightId, String departureDate, String departureTime, String arrivalDate, String arrivalTime, String price) {
        flightService.updateFlight(flightId, departureDate, departureTime, arrivalDate, arrivalTime, price);
    }

    public void deleteFlight(String id) {
        flightService.deleteFlight(id);
    }
}
