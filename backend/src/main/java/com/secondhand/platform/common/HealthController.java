package com.secondhand.platform.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "서비스 상태 확인 API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "서비스 상태 확인")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
