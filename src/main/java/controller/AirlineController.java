package controller;

import service.AirlineService;

public class AirlineController {
    private final AirlineService airlineService;

    public AirlineController(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    public void createAirline(String airlineData) {
        airlineService.createAirline(airlineData);
    }

    public String getAirline(String id) {
        return airlineService.getAirline(id);
    }

    public String getAllAirlines() {
        return airlineService.getAllAirlines();
    }

    public void updateAirline(String airlineData) {
        airlineService.updateAirline(airlineData);
    }

    public void deleteAirline(String id) {
        airlineService.deleteAirline(id);
    }
}
