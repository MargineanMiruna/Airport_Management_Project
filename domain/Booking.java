package domain;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {
    private int bookingId;
    private Flight flight;
    private Passenger passenger;
    private LocalDateTime bookingTime;
    private Seat seat;
    private List<Bag> bags;

    public Booking(int bookingId, Flight flight, Passenger passenger, LocalDateTime bookingTime, Seat seat, List<Bag> bags) {
        this.bookingId = bookingId;
        this.flight = flight;
        this.passenger = passenger;
        this.bookingTime = bookingTime;
        this.seat = seat;
        this.bags = bags;
    }

    public int getBookingId() {
        return bookingId;
    }

    public Flight getFlight() {
        return flight;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public Seat getSeat() {
        return seat;
    }

    public List<Bag> getBags() {
        return bags;
    }
}
