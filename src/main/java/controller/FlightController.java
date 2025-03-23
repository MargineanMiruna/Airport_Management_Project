package controller;

import service.FlightService;

public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    public void createFlight(String flightData) {
        flightService.createFlight(flightData);
    }

    public String getFlight(String id) {
        return flightService.getFlight(id);
    }

    public String getAllFlights() {
        return flightService.getAllFlights();
    }

    public void updateFlight(String flightData) {
        flightService.updateFlight(flightData);
    }

    public void deleteFlight(String id) {
        flightService.deleteFlight(id);
    }
}
