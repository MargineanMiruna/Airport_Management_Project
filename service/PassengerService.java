package service;

import domain.Passenger;
import repository.PassengerRepository;

import java.util.List;

public class PassengerService {
    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public void createPassenger(Passenger passenger) {
        passengerRepository.save(passenger);
    }

    public Passenger getPassenger(int id) {
        return passengerRepository.findById(id);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public void updatePassenger(Passenger passenger) {
        passengerRepository.update(passenger);
    }

    public void deletePassenger(int id) {
        passengerRepository.delete(id);
    }
}
