package service;

import domain.Passenger;
import repository.PassengerRepository;

import java.time.LocalDate;
import java.util.List;

public class PassengerService {
    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public void createPassenger(String firstName, String lastName, String email, String birthDate, String city, String country) {
        Passenger passenger = new Passenger(firstName, lastName, email, LocalDate.parse(birthDate), city, country);
        passengerRepository.save(passenger);
    }

    public Passenger getPassenger(String id) {
        return passengerRepository.findById(Integer.parseInt(id));
    }

    public Passenger getPassengerByEmail(String email) {
        return passengerRepository.findByEmail(email);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public void updatePassenger(String id, String firstName, String lastName, String email, String birthDate, String city, String country) {
        Passenger passenger = new Passenger(Integer.parseInt(id), firstName, lastName, email, LocalDate.parse(birthDate), city, country);
        passengerRepository.update(passenger);
    }

    public void deletePassenger(String id) {
        passengerRepository.delete(Integer.parseInt(id));
    }
}
