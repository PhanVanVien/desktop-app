package com.example.beetechdesktopapp.Models;

public class TokenSingleton {
    private static TokenSingleton instance;
    private String authToken;

    private TokenSingleton() {
    }

    public static TokenSingleton getInstance() {
        if (instance == null) {
            instance = new TokenSingleton();
        }
        return instance;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}