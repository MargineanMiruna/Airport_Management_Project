package domain;

import java.util.List;

public class Plane {
    private int planeId;
    private String planeCode;
    private Airline airline;
    private int numOfSeats;
    private List<Seat> seatList;

    public Plane(int planeId, String planeCode, Airline airline, int numOfSeats) {
        this.planeId = planeId;
        this.planeCode = planeCode;
        this.airline = airline;
        this.numOfSeats = numOfSeats;
    }

    public int getPlaneId() {
        return planeId;
    }

    public String getPlaneCode() {
        return planeCode;
    }

    public Airline getAirline() {
        return airline;
    }

    public int getNumOfSeats() {
        return numOfSeats;
    }

    public List<Seat> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }
}
