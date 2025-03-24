package controller;

import service.BookingService;

import java.util.List;

public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public void createBooking(String flightId, int passengerId, String seatId, List<String> bags) {
        bookingService.createBooking(flightId, passengerId, seatId, bags);
    }
}
