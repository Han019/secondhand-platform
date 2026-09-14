package com.secondhand.platform;

import com.secondhand.platform.auth.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SecondhandPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandPlatformApplication.class, args);

    }
}
