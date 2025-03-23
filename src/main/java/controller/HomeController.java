package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HomeController {
    private final AirlineController airlineController;
    private final AirportController airportController;
    private final FlightController flightController;
    private final PassengerController passengerController;
    private final PlaneController planeController;

    public HomeController(AirlineController airlineController, AirportController airportController, FlightController flightController, PassengerController passengerController, PlaneController planeController) {
        this.airlineController = airlineController;
        this.airportController = airportController;
        this.flightController = flightController;
        this.passengerController = passengerController;
        this.planeController = planeController;
    }

    public void run(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

        String airlineData = input.readLine();
        System.out.println("Received from client: " + airlineData);

        flightController.createFlight(airlineData);
        String savedAirlineData = flightController.getAllFlights();

        output.println(savedAirlineData);
        output.flush();
    }

}
