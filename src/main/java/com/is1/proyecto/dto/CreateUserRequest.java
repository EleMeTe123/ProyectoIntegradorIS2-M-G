package com.is1.proyecto.dto;

public class CreateUserRequest {
    private final String userName;
    private final String password;
    private final String rol;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Integer dni;
    private final String address;
    private final String phoneNumber;

    public CreateUserRequest(String userName, String password) {
        this(userName, password, "STUDENT", null, null, null, null,  null, null);
    }

    public CreateUserRequest(String userName, String password, String rol, String firstName,
                             String lastName, String email, Integer dni, String address, String phoneNumber) {
        this.userName = userName;
        this.password = password;
        this.rol = (rol != null) ? rol : "STUDENT";
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dni = dni;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Integer getDni() { return dni; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }

    public boolean isValid() {
        return getUserName() != null && !getUserName().trim().isEmpty()
            && getPassword() != null && !getPassword().trim().isEmpty();
    }
}
