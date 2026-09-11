package com.secondhand.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secondhandPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("당근클론 API")
                        .description("지역 기반 중고거래 플랫폼 백엔드 API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Secondhand Platform Team")
                                .url("https://github.com/Han019/secondhand-platform")));
    }
}
