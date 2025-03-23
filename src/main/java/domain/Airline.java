package domain;

public class Airline {
    private int airlineId;
    private String airlineName;
    private String email;
    private String phone;

    public Airline() {
    }

    public Airline(int airlineId, String airlineName, String email, String phone) {
        this.airlineId = airlineId;
        this.airlineName = airlineName;
        this.email = email;
        this.phone = phone;
    }

    public Airline(String airlineName, String email, String phone) {
        this.airlineName = airlineName;
        this.email = email;
        this.phone = phone;
    }

    public String toString() {
        return airlineName + ", " + email + ", " + phone;
    }

    public int getAirlineId() {
        return airlineId;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
