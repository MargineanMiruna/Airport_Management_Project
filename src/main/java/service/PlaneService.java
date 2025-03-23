package service;

import domain.Plane;
import domain.Seat;
import domain.SeatType;
import repository.AirlineRepository;
import repository.PlaneRepository;

import java.util.ArrayList;
import java.util.List;

public class PlaneService {
    private final PlaneRepository planeRepository;
    private final AirlineRepository airlineRepository;

    public PlaneService(PlaneRepository planeRepository, AirlineRepository airlineRepository) {
        this.planeRepository = planeRepository;
        this.airlineRepository = airlineRepository;
    }

    public void createPlane(String planeCode, String airlineId, String numOfSeats, List<String> seatTypes) {
        Plane plane = new Plane(planeCode, airlineRepository.findById(Integer.parseInt(airlineId)), Integer.parseInt(numOfSeats));
        List<Seat> seatList = createSeats(seatTypes, plane);
        plane.setSeatList(seatList);
        planeRepository.save(plane);
    }

    private List<Seat> createSeats(List<String> seatTypes, Plane plane) {
        List<Seat> seatList = new ArrayList<>();
        int row = 1;
        int seatCode = 'A';

        for (String seatType : seatTypes) {
            seatList.add(new Seat(
                    row + String.valueOf(seatCode),
                    plane,
                    SeatType.valueOf(seatType)
            ));
            if (seatCode == 'F') {
                row++;
                seatCode = 'A';
            } else {
                seatCode ++;
            }
        }

        return seatList;
    }

    public Plane getPlane(String id) {
        return planeRepository.findById(Integer.parseInt(id));
    }

    public List<Plane> getAllPlanes() {
        return planeRepository.findAll();
    }

    public void updatePlane(String id, String planeCode, String airlineId, String numOfSeats) {
        Plane plane = getPlane(id);
        Plane updatedPlane = new Plane(planeCode, airlineRepository.findById(Integer.parseInt(airlineId)), Integer.parseInt(numOfSeats));
        updatedPlane.setSeatList(plane.getSeatList());
        planeRepository.update(updatedPlane);
    }

    public void deletePlane(String id) {
        planeRepository.delete(Integer.parseInt(id));
    }
}
