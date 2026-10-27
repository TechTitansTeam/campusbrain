package com.campusbrain.dto;

public class LoginResponse {
    private String token;
    private String username;
    private String fullName;
    private String email;
    private long expiresInSeconds;

    public LoginResponse() {}

    public LoginResponse(String token, String username, String fullName, String email, long expiresInSeconds) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
