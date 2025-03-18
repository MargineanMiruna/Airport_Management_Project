package service;

import domain.Booking;
import repository.BookingRepository;

import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void createBooking(Booking booking) {
        bookingRepository.save(booking);
    }

    public Booking getBooking(int id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public void updateBooking(Booking booking) {
        bookingRepository.update(booking);
    }

    public void deleteBooking(int id) {
        bookingRepository.delete(id);
    }
}
