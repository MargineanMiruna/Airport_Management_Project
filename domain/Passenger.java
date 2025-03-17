package domain;

import java.time.LocalDate;

public class Passenger {
    private int passengerID;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String city;
    private String country;

    public Passenger(String firstName, String lastName, String email, LocalDate birthDate, String city, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.city = city;
        this.country = country;
    }
}
