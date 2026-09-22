package com.kineplan.auth.api;

public record TokenResponse(String accessToken, String refreshToken, long accessTokenExpiresInSeconds) {
}