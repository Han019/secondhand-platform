package com.secondhand.platform.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;


import java.time.Duration;


@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpireTime,
        Duration refreshTokenExpireTime) {
}
