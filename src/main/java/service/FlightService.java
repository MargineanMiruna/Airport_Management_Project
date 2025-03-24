package service;

import domain.Flight;
import domain.Seat;
import repository.AirlineRepository;
import repository.AirportRepository;
import repository.FlightRepository;

import repository.PlaneRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightService {
    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final PlaneRepository planeRepository;

    public FlightService(FlightRepository flightRepository, AirlineRepository airlineRepository, AirportRepository airportRepository, PlaneRepository planeRepository) {
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
        this.airportRepository = airportRepository;
        this.planeRepository = planeRepository;
    }

    public void createFlight(String airlineId, String planeId, String origin, String destination, String departureDate, String departureTime, String arrivalDate, String arrivalTime, String price) {
        LocalDateTime departure = LocalDateTime.parse(departureDate +"T"+ departureTime);
        LocalDateTime arrival = LocalDateTime.parse(arrivalDate +"T"+ arrivalTime);
        Flight flight = new Flight(airlineRepository.findById(Integer.parseInt(airlineId)), planeRepository.findById(Integer.parseInt(planeId)), departure, arrival, airportRepository.findById(Integer.parseInt(origin)), airportRepository.findById(Integer.parseInt(destination)), Double.parseDouble(price));
        flightRepository.save(flight);
    }

    public Flight getFlight(String id) {
        return flightRepository.findById(Integer.parseInt(id));
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public List<Flight> getFilteredFlights(String origin, String destination, String date, String airline, String sortBy) {
        List<Flight> flights = flightRepository.findAll();

        if (!origin.equals("Any")) {
            flights = flights.stream().filter(f -> f.getOrigin().getAirportId() == Integer.parseInt(origin)).toList();
        }
        if (!destination.equals("Any")) {
            flights = flights.stream().filter(f -> f.getDestination().getAirportId() == Integer.parseInt(destination)).toList();
        }
        if (!date.equals("Any")) {
            flights = flights.stream().filter(f -> f.getDeparture().toLocalDate() == LocalDate.parse(date)).toList();
        }
        if (!airline.equals("Any")) {
            flights = flights.stream().filter(f -> f.getAirline().getAirlineId() == Integer.parseInt(airline)).toList();
        }

        if (sortBy.equals("Lowest price")) {
            flights.sort(Comparator.comparingDouble(Flight::getPrice));
        }
        else if (sortBy.equals("Highest price")) {
            flights.sort(Comparator.comparingDouble(Flight::getPrice).reversed());
        }
        else if (sortBy.equals("Departure time")) {
            flights.sort(Comparator.comparing(Flight::getDeparture).reversed());
        }
        else if (sortBy.equals("Duration")) {
            flights.sort(Comparator.comparingLong(Flight::getDuration));
        }

        return flights;
    }

    public void updateFlight(String id, String departureDate, String departureTime, String arrivalDate, String arrivalTime, String price) {
        Flight flight = flightRepository.findById(Integer.parseInt(id));
        LocalDateTime departure = LocalDateTime.parse(departureDate +"T"+ departureTime);
        LocalDateTime arrival = LocalDateTime.parse(arrivalDate +"T"+ arrivalTime);
        flight.setDeparture(departure);
        flight.setArrival(arrival);
        flight.setPrice(Double.parseDouble(price));
        flightRepository.update(flight);
    }

    public void deleteFlight(String id) {
        flightRepository.delete(Integer.parseInt(id));
    }
}
