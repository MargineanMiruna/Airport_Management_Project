package domain;

import java.time.LocalDate;

public class Passenger {
    private int passengerId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String city;
    private String country;

    public Passenger(int passengerId, String firstName, String lastName, String email, LocalDate birthDate, String city, String country) {
        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.city = city;
        this.country = country;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
