package controller;

import domain.Passenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {
    private final AirlineController airlineController;
    private final AirportController airportController;
    private final FlightController flightController;
    private final PassengerController passengerController;
    private final PlaneController planeController;
    private final BookingController bookingController;
    private final Scanner scanner;

    public HomeController(AirlineController airlineController, AirportController airportController, FlightController flightController, PassengerController passengerController, PlaneController planeController, BookingController bookingController) {
        this.airlineController = airlineController;
        this.airportController = airportController;
        this.flightController = flightController;
        this.passengerController = passengerController;
        this.planeController = planeController;
        this.bookingController = bookingController;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            System.out.println("Welcome to AirSync! \n1. Management \n2. Customer");
            String option = scanner.nextLine();
            Passenger passenger = null;

            switch (option) {
                case "1": {
                    boolean topLoop = true;
                    while (topLoop) {
                        System.out.println("Management Menu: \n1. Manage Flights \n2. Manage Planes \n3. Manage Airports \n4. Manage Airlines");
                        option = scanner.nextLine();

                        switch (option) {
                            case "1": {
                                boolean loop = true;
                                while (loop) {
                                    System.out.println("Modify Flight: \n1. Create \n2. Update \n3. Delete \n4. Back");
                                    option = scanner.nextLine();

                                    switch (option) {
                                        case "1": {
                                            System.out.println("Choose Airline: ");
                                            airlineController.getAllAirlines();
                                            String airlineId = scanner.nextLine();
                                            System.out.println("Choose Plane: ");
                                            planeController.getAllPlanes();
                                            String planeId = scanner.nextLine();
                                            System.out.println("Choose Origin: ");
                                            airportController.getAllAirports();
                                            String origin = scanner.nextLine();
                                            System.out.println("Choose Destination: ");
                                            airportController.getAllAirports();
                                            String destination = scanner.nextLine();
                                            System.out.println("Enter Departure Date (YYYY-MM-DD): ");
                                            String departureDate = scanner.nextLine();
                                            System.out.println("Enter Departure Time (HH:MM:SS): ");
                                            String departureTime = scanner.nextLine();
                                            System.out.println("Enter Arrival Date (YYYY-MM-DD): ");
                                            String arrivalDate = scanner.nextLine();
                                            System.out.println("Enter Arrival Time (HH:MM:SS): ");
                                            String arrivalTime = scanner.nextLine();
                                            System.out.println("Enter Price: ");
                                            String price = scanner.nextLine();

                                            flightController.createFlight(airlineId, planeId, origin, destination, departureDate, departureTime, arrivalDate, arrivalTime, price);
                                            break;
                                        }
                                        case "2": {
                                            System.out.println("Choose Flight: ");
                                            flightController.getAllFlights();
                                            String flightId = scanner.nextLine();
                                            System.out.println("Enter Departure Date (YYYY-MM-DD): ");
                                            String departureDate = scanner.nextLine();
                                            System.out.println("Enter Departure Time (HH:MM:SS): ");
                                            String departureTime = scanner.nextLine();
                                            System.out.println("Enter Arrival Date (YYYY-MM-DD): ");
                                            String arrivalDate = scanner.nextLine();
                                            System.out.println("Enter Arrival Time (HH:MM:SS): ");
                                            String arrivalTime = scanner.nextLine();
                                            System.out.println("Enter Price: ");
                                            String price = scanner.nextLine();

                                            flightController.updateFlight(flightId, departureDate, departureTime, arrivalDate, arrivalTime, price);
                                            break;
                                        }
                                        case "3": {
                                            System.out.println("Choose Flight: ");
                                            flightController.getAllFlights();
                                            String flightId = scanner.nextLine();

                                            flightController.deleteFlight(flightId);
                                        }
                                        default: {
                                            loop = false;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case "2": {
                                boolean loop = true;
                                while (loop) {
                                    System.out.println("Modify Plane: \n1. Create \n2. Update \n3. Delete \n4. Back");
                                    option = scanner.nextLine();

                                    switch (option) {
                                        case "1": {
                                            System.out.println("Enter Code: ");
                                            String code = scanner.nextLine();
                                            System.out.println("Choose Airline: ");
                                            airlineController.getAllAirlines();
                                            String airlineId = scanner.nextLine();
                                            System.out.println("Enter Number of Seats: ");
                                            String numberOfSeats = scanner.nextLine();
                                            System.out.println("Enter First Class Limit (1 - ?): ");
                                            String firstClass = scanner.nextLine();
                                            System.out.println("Enter Business Class Limit (" + firstClass + " - ?): ");
                                            String businessClass = scanner.nextLine();
                                            System.out.println("Enter Premium Economy Limit (" + businessClass + " - ?): ");
                                            String premiumEconomy = scanner.nextLine();

                                            planeController.createPlane(code, airlineId, numberOfSeats, firstClass, businessClass, premiumEconomy);
                                            break;
                                        }
                                        case "2": {
                                            System.out.println("Choose Plane: ");
                                            planeController.getAllPlanes();
                                            String planeId = scanner.nextLine();
                                            System.out.println("Enter Code: ");
                                            String code = scanner.nextLine();

                                            planeController.updatePlane(planeId, code);
                                            break;
                                        }
                                        case "3": {
                                            System.out.println("Choose Plane: ");
                                            planeController.getAllPlanes();
                                            String planeId = scanner.nextLine();

                                            planeController.deletePlane(planeId);
                                            break;
                                        }
                                        default: {
                                            loop = false;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case "3": {
                                boolean loop = true;
                                while (loop) {
                                    System.out.println("Modify Airport: \n1. Create \n2. Update \n3. Delete \n4. Back");
                                    option = scanner.nextLine();

                                    switch (option) {
                                        case "1": {
                                            System.out.println("Enter Name: ");
                                            String name = scanner.nextLine();
                                            System.out.println("Enter Code: ");
                                            String code = scanner.nextLine();
                                            System.out.println("Enter City: ");
                                            String city = scanner.nextLine();
                                            System.out.println("Enter Country: ");
                                            String country = scanner.nextLine();

                                            airportController.createAirport(name, code, city, country);
                                            break;
                                        }
                                        case "2": {
                                            System.out.println("Choose Airport: ");
                                            airportController.getAllAirports();
                                            String airportId = scanner.nextLine();
                                            System.out.println("Enter Name: ");
                                            String name = scanner.nextLine();
                                            System.out.println("Enter Code: ");
                                            String code = scanner.nextLine();

                                            airportController.updateAirport(airportId, name, code);
                                            break;
                                        }
                                        case "3": {
                                            System.out.println("Choose Airport: ");
                                            airportController.getAllAirports();
                                            String airportId = scanner.nextLine();

                                            airportController.deleteAirport(airportId);
                                            break;
                                        }
                                        default: {
                                            loop = false;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case "4": {
                                boolean loop = true;
                                while (loop) {
                                    System.out.println("Modify Airline: \n1. Create \n2. Update \n3. Delete \n4. Back");
                                    option = scanner.nextLine();

                                    switch (option) {
                                        case "1": {
                                            System.out.println("Enter Name: ");
                                            String name = scanner.nextLine();
                                            System.out.println("Enter Email: ");
                                            String email = scanner.nextLine();
                                            System.out.println("Enter Phone: ");
                                            String phone = scanner.nextLine();

                                            airlineController.createAirline(name, email, phone);
                                            break;
                                        }
                                        case "2": {
                                            System.out.println("Choose Airline: ");
                                            airlineController.getAllAirlines();
                                            String airlineId = scanner.nextLine();
                                            System.out.println("Enter Name: ");
                                            String name = scanner.nextLine();
                                            System.out.println("Enter Email: ");
                                            String email = scanner.nextLine();
                                            System.out.println("Enter Phone: ");
                                            String phone = scanner.nextLine();

                                            airlineController.updateAirline(airlineId, name, email, phone);
                                            break;
                                        }
                                        case "3": {
                                            System.out.println("Choose Airline: ");
                                            airlineController.getAllAirlines();
                                            String airlineId = scanner.nextLine();

                                            airlineController.deleteAirline(airlineId);
                                            break;
                                        }
                                        default: {
                                            loop = false;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            default: {
                                topLoop = false;
                                break;
                            }
                        }
                    }
                    break;
                }
                case "2": {
                    boolean topLoop = true;
                    while (topLoop) {
                        System.out.println("Log in or register: \n1. Login \n2. Register \n3. Back");
                        option = scanner.nextLine();

                        switch (option) {
                            case "1": {
                                System.out.println("Enter Email: ");
                                String email = scanner.nextLine();

                                passenger = passengerController.getPassengerByEmail(email);
                                break;
                            }
                            case "2": {
                                System.out.println("Enter First Name: ");
                                String firstName = scanner.nextLine();
                                System.out.println("Enter Last Name: ");
                                String lastName = scanner.nextLine();
                                System.out.println("Enter Email: ");
                                String email = scanner.nextLine();
                                System.out.println("Enter Birth Date: ");
                                String birthDate = scanner.nextLine();
                                System.out.println("Enter City: ");
                                String city = scanner.nextLine();
                                System.out.println("Enter Country: ");
                                String country = scanner.nextLine();

                                passengerController.createPassenger(firstName, lastName, email, birthDate, city, country);
                                passenger = passengerController.getPassengerByEmail(email);
                                break;
                            }
                            default: {
                                topLoop = false;
                                break;
                            }
                        }

                        if (topLoop) {
                            break;
                        }

                        System.out.println("Search Flight: ");
                        System.out.println("Choose Origin: ");
                        airportController.getAllAirports();
                        String origin = scanner.nextLine();
                        System.out.println("Choose Destination: ");
                        airportController.getAllAirports();
                        String destination = scanner.nextLine();
                        System.out.println("Enter Date: ");
                        String date = scanner.nextLine();
                        System.out.println("Choose Airline: ");
                        airlineController.getAllAirlines();
                        String airline = scanner.nextLine();
                        System.out.println("Sort by: \n1) Lowest price \n2) Highest price \n3) Departure time \n4) Duration \n5) Nothing ");
                        String sortBy = scanner.nextLine();

                        flightController.getFilteredFlights(origin, destination, date, airline, sortBy);

                        System.out.println("Book Flight: ");
                        String flightId = scanner.nextLine();
                        System.out.println("Choose Seat: ");
                        flightController.getAvailableSeats(flightId);
                        String seatId = scanner.nextLine();
                        System.out.println("Choose Bags (enter your options and then type 0): \n1) Carry on bag \n2) Cabin bag \n3) Checked bag");
                        List<String> bags = new ArrayList<>();
                        String bag = scanner.nextLine();
                        while(!Objects.equals(bag, "0\n")) {
                            bags.add(bag);
                            bag = scanner.nextLine();
                        }

                        bookingController.createBooking(flightId, passenger.getPassengerId(), seatId, bags);
                        break;
                    }
                }
            }
        }
    }

    public void performConcurrentTransactions() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(this::transaction1);
        executor.submit(this::transaction2);

        executor.shutdown();
    }

    private void transaction1() {
        try {
            bookingController.createBooking("1", 1, "100", List.of("carry_on", "checked_bag"));
            System.out.println("Transaction 1 executing...");
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Transaction 1 failed: " + e.getMessage());
        }
    }

    private void transaction2() {
        try {
            bookingController.createBooking("3", 2, "303", List.of("carry_on"));
            System.out.println("Transaction 2 executing...");
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Transaction 2 failed: " + e.getMessage());
        }
    }

}
