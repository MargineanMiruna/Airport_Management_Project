package app;

import controller.*;
import database.DatabaseConnection;
import repository.*;
import service.*;

import java.sql.Connection;
import java.sql.SQLException;

public class AirportManagementSystem {
    public static void main(String[] args) {
        Connection postgresConn = DatabaseConnection.getPostgresConnection();
        Connection myConn = DatabaseConnection.getMySQLConnection();
        try {
            postgresConn.setAutoCommit(false);
            myConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        AirlineRepository airlineRepository = new AirlineRepository(postgresConn, myConn);
        AirlineService airlineService = new AirlineService(airlineRepository);
        AirlineController airlineController = new AirlineController(airlineService);
        AirportRepository airportRepository = new AirportRepository(postgresConn, myConn);
        AirportService airportService = new AirportService(airportRepository);
        AirportController airportController = new AirportController(airportService);
        PlaneRepository planeRepository = new PlaneRepository(postgresConn, myConn);
        PlaneService planeService = new PlaneService(planeRepository, airlineRepository);
        PlaneController planeController = new PlaneController(planeService);
        FlightRepository flightRepository = new FlightRepository(postgresConn, myConn);
        FlightService flightService = new FlightService(flightRepository, airlineRepository, airportRepository, planeRepository);
        FlightController flightController = new FlightController(flightService);
        PassengerRepository passengerRepository = new PassengerRepository(postgresConn, myConn);
        PassengerService passengerService = new PassengerService(passengerRepository);
        PassengerController passengerController = new PassengerController(passengerService);
        BookingRepository bookingRepository = new BookingRepository(postgresConn, myConn);
        BookingService bookingService = new BookingService(bookingRepository, flightRepository, passengerRepository);
        BookingController bookingController = new BookingController(bookingService);
        HomeController homeController = new HomeController(airlineController, airportController, flightController, passengerController, planeController, bookingController);

        homeController.run();
    }
}
