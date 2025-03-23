package controller;

import service.PassengerService;

public class PassengerController {
    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    public void createPassenger(String passengerData) {
        passengerService.createPassenger(passengerData);
    }

    public String getPassenger(String id) {
        return passengerService.getPassenger(id);
    }

    public String getAllPassengers() {
        return passengerService.getAllPassengers();
    }

    public void updatePassenger(String passengerData) {
        passengerService.updatePassenger(passengerData);
    }

    public void deletePassenger(String id) {
        passengerService.deletePassenger(id);
    }
}
