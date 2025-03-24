package service;

import domain.Bag;
import domain.Booking;
import domain.Flight;
import domain.Seat;
import repository.BookingRepository;
import repository.FlightRepository;
import repository.PassengerRepository;

import java.time.LocalDateTime;
import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;

    public BookingService(BookingRepository bookingRepository, FlightRepository flightRepository, PassengerRepository passengerRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
    }

    public void createBooking(String flightId, int passengerId, String seatId, List<String> bagsString) {
        Flight flight = flightRepository.findById(Integer.parseInt(flightId));
        Seat seat = flight.getAvailableSeats().stream().filter(s -> s.getSeatId() == Integer.parseInt(seatId)).findFirst().get();
        List<Bag> bags = bagsString.stream().map(Bag::valueOf).toList();
        Booking booking = new Booking(flight, passengerRepository.findById(passengerId), LocalDateTime.now(), seat, bags);
        bookingRepository.save(booking);
    }

    public Booking getBooking(String id) {
        return bookingRepository.findById(Integer.parseInt(id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public void updateBooking(String id, String flightId, String passengerId, String bookingTime, String seatId, List<String> bagsString) {
        Flight flight = flightRepository.findById(Integer.parseInt(flightId));
        Seat seat = flight.getAvailableSeats().stream().filter(s -> s.getSeatId() == Integer.parseInt(seatId)).findFirst().get();
        List<Bag> bags = bagsString.stream().map(Bag::valueOf).toList();
        Booking booking = new Booking(Integer.parseInt(id), flight, passengerRepository.findById(Integer.parseInt(passengerId)), LocalDateTime.parse(bookingTime), seat, bags);
        bookingRepository.update(booking);
    }

    public void deleteBooking(String  id) {
        bookingRepository.delete(Integer.parseInt(id));
    }
}
