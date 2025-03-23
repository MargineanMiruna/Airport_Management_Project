package service;

import domain.Airline;
import repository.AirlineRepository;

import java.util.List;

public class AirlineService {
    private final AirlineRepository airlineRepository;

    public AirlineService(AirlineRepository airlineRepository) {
        this.airlineRepository = airlineRepository;
    }

    public void createAirline(String airlineName, String email, String phone) {
        Airline airline = new Airline(airlineName, email, phone);
        airlineRepository.save(airline);
    }

    public Airline getAirline(String id) {
        return airlineRepository.findById(Integer.parseInt(id));
    }

    public List<Airline> getAllAirlines() {
        return airlineRepository.findAll();
    }

    public void updateAirline(String id, String airlineName, String email, String phone) {
        Airline airline = new Airline(Integer.parseInt(id), airlineName, email, phone);
        airlineRepository.update(airline);
    }

    public void deleteAirline(String id) {
        airlineRepository.delete(Integer.parseInt(id));
    }
}
