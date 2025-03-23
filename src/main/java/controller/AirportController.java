package controller;

import service.AirportService;

public class AirportController {
    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    public void createAirport(String airportData) {
        airportService.createAirport(airportData);
    }

    public String getAirport(String id) {
        return airportService.getAirport(id);
    }

    public String getAllAirports() {
        return airportService.getAllAirports();
    }

    public void updateAirport(String airportData) {
        airportService.updateAirport(airportData);
    }

    public void deleteAirport(String id) {
        airportService.deleteAirport(id);
    }
}
