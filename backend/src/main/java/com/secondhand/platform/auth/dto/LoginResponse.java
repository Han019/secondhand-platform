package com.secondhand.platform.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {

}
