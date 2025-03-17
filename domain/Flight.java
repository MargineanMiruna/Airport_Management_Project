package domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private int flightID;
    private Airline airline;
    private Plane plane;
    private LocalTime departure;
    private LocalTime arrival;
    private Airport origin;
    private Airport destination;
    private List<Seat> availableSeats;

    public Flight(Airline airline, Plane plane, LocalTime departure, LocalTime arrival, Airport origin, Airport destination) {
        this.airline = airline;
        this.plane = plane;
        this.departure = departure;
        this.arrival = arrival;
        this.origin = origin;
        this.destination = destination;
        this.availableSeats = this.plane.getSeatList();
    }

    public void setAvailableSeats(List<Seat> availableSeats) {
        this.availableSeats = availableSeats;
    }
}
