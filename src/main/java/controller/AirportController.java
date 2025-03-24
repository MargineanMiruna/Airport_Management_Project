package controller;

import domain.Airport;
import service.AirportService;

import java.util.List;

public class AirportController {
    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    public void createAirport(String airportName, String airportCode, String city, String country) {
        airportService.createAirport(airportName, airportCode, country, city);
    }

    public void getAllAirports() {
        List<Airport> airports = airportService.getAllAirports();
        for (Airport airport : airports) {
            System.out.println(airport.getAirportId() + ") " + airport.getAirportName());
        }
    }

    public void updateAirport(String id, String airportName, String airportCode) {
        airportService.updateAirport(id, airportName, airportCode);
    }

    public void deleteAirport(String id) {
        airportService.deleteAirport(id);
    }
}
