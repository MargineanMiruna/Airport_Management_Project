package domain;

public class Seat {
    private int seatId;
    private String seatNr;
    private Plane plane;
    private SeatType seatType;

    public Seat(int seatId, String seatNr, Plane plane, SeatType seatType) {
        this.seatId = seatId;
        this.seatNr = seatNr;
        this.plane = plane;
        this.seatType = seatType;
    }

    public int getSeatId() {
        return seatId;
    }

    public String getSeatNr() {
        return seatNr;
    }

    public Plane getPlane() {
        return plane;
    }

    public SeatType getSeatType() {
        return seatType;
    }
}
