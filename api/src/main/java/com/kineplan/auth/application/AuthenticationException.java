package com.kineplan.auth.application;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Authentication failed");
    }
}