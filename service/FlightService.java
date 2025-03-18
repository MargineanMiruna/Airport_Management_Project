package service;

import domain.Flight;
import repository.FlightRepository;

import java.util.List;

public class FlightService {
    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public void createFlight(Flight flight) {
        flightRepository.save(flight);
    }

    public Flight getFlight(int id) {
        return flightRepository.findById(id);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public void updateFlight(Flight flight) {
        flightRepository.update(flight);
    }

    public void deleteFlight(int id) {
        flightRepository.delete(id);
    }
}
