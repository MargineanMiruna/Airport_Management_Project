package controller;

import domain.Airline;
import service.AirlineService;

import java.util.List;

public class AirlineController {
    private final AirlineService airlineService;

    public AirlineController(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    public void createAirline(String airlineName, String email, String phone) {
        airlineService.createAirline(airlineName, email, phone);
    }

    public Airline getAirline(String id) {
        return airlineService.getAirline(id);
    }

    public void getAllAirlines() {
        List<Airline> airlines = airlineService.getAllAirlines();
        for (Airline airline : airlines) {
            System.out.println(airline.getAirlineId() + ")" + airline.getAirlineName());
        }
    }

    public void updateAirline(String id, String airlineName, String email, String phone) {
        airlineService.updateAirline(id, airlineName, email, phone);
    }

    public void deleteAirline(String id) {
        airlineService.deleteAirline(id);
    }
}
