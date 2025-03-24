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

    public void createPlane(String planeCode, String airlineId, String numOfSeats, String firstClass, String businessClass, String premiumEconomy) {
        Plane plane = new Plane(planeCode, airlineRepository.findById(Integer.parseInt(airlineId)), Integer.parseInt(numOfSeats));
        List<Seat> seatList = createSeats(Integer.parseInt(numOfSeats), Integer.parseInt(firstClass), Integer.parseInt(businessClass), Integer.parseInt(premiumEconomy), plane);
        plane.setSeatList(seatList);
        planeRepository.save(plane);
    }

    private List<Seat> createSeats(int numOfSeats, int firstClass, int businessClass, int premiumEconomy, Plane plane) {
        List<Seat> seatList = new ArrayList<>();
        int row = 1;
        int seatCode = 'A';
        SeatType seatType;

        for (int i = 1; i <= numOfSeats; i++) {
            if (i < firstClass) {
                seatType = SeatType.first;
            }
            else if (i < businessClass) {
                seatType = SeatType.business;
            }
            else if (i < premiumEconomy) {
                seatType = SeatType.premium_economy;
            }
            else {
                seatType = SeatType.economy;
            }

            seatList.add(new Seat(
                    row + String.valueOf(seatCode),
                    plane,
                    seatType
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

    public void updatePlane(String id, String planeCode) {
        Plane plane = planeRepository.findById(Integer.parseInt(id));
        plane.setPlaneCode(planeCode);
        planeRepository.update(plane);
    }

    public void deletePlane(String id) {
        planeRepository.delete(Integer.parseInt(id));
    }
}
