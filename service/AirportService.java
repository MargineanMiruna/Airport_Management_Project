package service;

import domain.Airport;
import repository.AirportRepository;

import java.util.List;

public class AirportService {
    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public void createAirport(Airport airport) {
        airportRepository.save(airport);
    }

    public Airport getAirport(int id) {
        return airportRepository.findById(id);
    }

    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    public void updateAirport(Airport airport) {
        airportRepository.update(airport);
    }

    public void deleteAirport(int id) {
        airportRepository.delete(id);
    }
}
