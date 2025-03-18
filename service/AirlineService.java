package service;

import domain.Airline;
import repository.AirlineRepository;

import java.util.List;

public class AirlineService {
    private final AirlineRepository airlineRepository;

    public AirlineService(AirlineRepository airlineRepository) {
        this.airlineRepository = airlineRepository;
    }

    public void createAirline(Airline airline) {
        airlineRepository.save(airline);
    }

    public Airline getAirline(int id) {
        return airlineRepository.findById(id);
    }

    public List<Airline> getAllAirlines() {
        return airlineRepository.findAll();
    }

    public void updateAirline(Airline airline) {
        airlineRepository.update(airline);
    }

    public void deleteAirline(int id) {
        airlineRepository.delete(id);
    }
}
