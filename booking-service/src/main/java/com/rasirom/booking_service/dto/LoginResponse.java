package com.rasirom.booking_service.dto;

public class LoginResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;

    public LoginResponse(String token, String email, String firstName, String lastName) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
