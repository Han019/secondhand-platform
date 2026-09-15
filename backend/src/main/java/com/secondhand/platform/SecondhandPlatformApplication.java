package com.secondhand.platform;

import com.secondhand.platform.auth.jwt.JwtProperties;
import com.secondhand.platform.productimage.config.ImageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, ImageProperties.class})
public class SecondhandPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandPlatformApplication.class, args);

    }
}
