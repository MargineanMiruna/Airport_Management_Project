package controller;

import domain.Passenger;
import service.PassengerService;

public class PassengerController {
    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    public void createPassenger(String firstName, String lastName, String email, String birthDate, String city, String country) {
        passengerService.createPassenger(firstName, lastName, email, birthDate, city, country);
    }

    public Passenger getPassengerByEmail(String email) {
        return passengerService.getPassengerByEmail(email);
    }

    public void updatePassenger(String id, String firstName, String lastName, String email, String birthDate, String city, String country) {
        passengerService.updatePassenger(id, firstName, lastName, email, birthDate, city, country);
    }

    public void deletePassenger(String id) {
        passengerService.deletePassenger(id);
    }
}
