package com.is1.proyecto.dto;

public class LoginRequest {
    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public boolean isValid() {
        return username != null && !username.trim().isEmpty()
            && password != null && !password.trim().isEmpty();
    }
}
