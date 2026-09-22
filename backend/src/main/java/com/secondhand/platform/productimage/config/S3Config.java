package com.secondhand.platform.productimage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config {
    private final ImageProperties imageProperties;

    @Bean
    public S3Client s3Client(){
        return S3Client.builder()
                .endpointOverride(URI.create(imageProperties.endpoint()))
                .region(Region.of(imageProperties.region()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        imageProperties.accessKey(),
                                        imageProperties.secretKey()
                                )
                        )
                ).forcePathStyle(true).build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(imageProperties.endpoint()))
                .region(Region.of(imageProperties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                imageProperties.accessKey(),
                                imageProperties.secretKey()
                        )))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .checksumValidationEnabled(false)
                        .build())
                .build();
    }

}
