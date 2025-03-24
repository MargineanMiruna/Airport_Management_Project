package domain;

public class Airport {
    private int airportId;
    private String airportName;
    private String airportCode;
    private String city;
    private String country;

    public Airport(int airportId, String airportName, String airportCode, String city, String country) {
        this.airportId = airportId;
        this.airportName = airportName;
        this.airportCode = airportCode;
        this.city = city;
        this.country = country;
    }

    public Airport(String airportName, String airportCode, String city, String country) {
        this.airportName = airportName;
        this.airportCode = airportCode;
        this.city = city;
        this.country = country;
    }

    public int getAirportId() {
        return airportId;
    }

    public String getAirportName() {
        return airportName;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }
}
