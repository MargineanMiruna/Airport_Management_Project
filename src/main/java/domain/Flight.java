package domain;

import java.time.LocalDateTime;
import java.util.List;

public class Flight {
    private int flightId;
    private Airline airline;
    private Plane plane;
    private LocalDateTime departure;
    private LocalDateTime arrival;
    private Airport origin;
    private Airport destination;
    private Double price;
    private List<Seat> availableSeats;

    public Flight() {
    }

    public Flight(int flightId, Airline airline, Plane plane, LocalDateTime departure, LocalDateTime arrival, Airport origin, Airport destination, Double price) {
        this.flightId = flightId;
        this.airline = airline;
        this.plane = plane;
        this.departure = departure;
        this.arrival = arrival;
        this.origin = origin;
        this.destination = destination;
        this.price = price;
        this.availableSeats = this.plane.getSeatList();
    }

    public Flight(Airline airline, Plane plane, LocalDateTime departure, LocalDateTime arrival, Airport origin, Airport destination, Double price) {
        this.airline = airline;
        this.plane = plane;
        this.departure = departure;
        this.arrival = arrival;
        this.origin = origin;
        this.destination = destination;
        this.price = price;
        this.availableSeats = plane.getSeatList();
    }

    public int getFlightId() {
        return flightId;
    }

    public Airline getAirline() {
        return airline;
    }

    public Plane getPlane() {
        return plane;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public Airport getOrigin() {
        return origin;
    }

    public Airport getDestination() {
        return destination;
    }

    public Double getPrice() {
        return price;
    }

    public List<Seat> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(List<Seat> availableSeats) {
        this.availableSeats = availableSeats;
    }
}
