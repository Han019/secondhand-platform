package com.secondhand.platform.productimage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record ImageProperties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        String bucket
) {
}
