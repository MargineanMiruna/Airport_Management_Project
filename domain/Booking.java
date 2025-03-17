package domain;

import java.time.LocalTime;
import java.util.List;

public class Booking {
    private int bookingID;
    private Flight flight;
    private Passenger passenger;
    private LocalTime bookingTime;
    private Seat seat;
    private List<Bag> bags;

    public Booking(Flight flight, Passenger passenger, LocalTime bookingTime, Seat seat, List<Bag> bags) {
        this.flight = flight;
        this.passenger = passenger;
        this.bookingTime = bookingTime;
        this.seat = seat;
        this.bags = bags;
    }
}
