package service;

import domain.Airport;
import repository.AirportRepository;

import java.util.List;

public class AirportService {
    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public void createAirport(String airportName, String airportCode, String city, String country) {
        Airport airport = new Airport(airportName, airportCode, city, country);
        airportRepository.save(airport);
    }

    public Airport getAirport(String id) {
        return airportRepository.findById(Integer.parseInt(id));
    }

    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    public void updateAirport(String id, String airportName, String airportCode) {
        Airport airport = airportRepository.findById(Integer.parseInt(id));
        airport.setAirportName(airportName);
        airport.setAirportCode(airportCode);
        airportRepository.update(airport);
    }

    public void deleteAirport(String id) {
        airportRepository.delete(Integer.parseInt(id));
    }
}
