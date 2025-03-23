package service;

import domain.Flight;
import domain.Seat;
import repository.AirlineRepository;
import repository.AirportRepository;
import repository.FlightRepository;

import repository.PlaneRepository;

import java.time.LocalDateTime;
import java.util.List;

public class FlightService {
    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final PlaneRepository planeRepository;

    public FlightService(FlightRepository flightRepository, AirlineRepository airlineRepository, AirportRepository airportRepository, PlaneRepository planeRepository) {
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
        this.airportRepository = airportRepository;
        this.planeRepository = planeRepository;
    }

    public void createFlight(String airlineId, String planeId, String departure, String arrival, String origin, String destination, String price) {
        Flight flight = new Flight(airlineRepository.findById(Integer.parseInt(airlineId)), planeRepository.findById(Integer.parseInt(planeId)), LocalDateTime.parse(departure), LocalDateTime.parse(arrival), airportRepository.findById(Integer.parseInt(origin)), airportRepository.findById(Integer.parseInt(destination)), Double.parseDouble(price));
        flightRepository.save(flight);
    }

    public Flight getFlight(String id) {
        return flightRepository.findById(Integer.parseInt(id));
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public void updateFlight(String id, String airlineId, String planeId, String departure, String arrival, String origin, String destination, String price) {
        Flight flight = new Flight(Integer.parseInt(id), airlineRepository.findById(Integer.parseInt(airlineId)), planeRepository.findById(Integer.parseInt(planeId)), LocalDateTime.parse(departure), LocalDateTime.parse(arrival), airportRepository.findById(Integer.parseInt(origin)), airportRepository.findById(Integer.parseInt(destination)), Double.parseDouble(price));
        flightRepository.update(flight);
    }

    public void updateAvailableSeats(String id, List<Seat> NonAvailableSeats) {
        Flight flight = flightRepository.findById(Integer.parseInt(id));
        List<Seat> AvailableSeats = flight.getAvailableSeats();

        for (Seat seat : NonAvailableSeats) {
            AvailableSeats.remove(seat);
        }

        flight.setAvailableSeats(AvailableSeats);
        flightRepository.updateAvailableSeats(flight);
    }

    public void deleteFlight(String id) {
        flightRepository.delete(Integer.parseInt(id));
    }
}
