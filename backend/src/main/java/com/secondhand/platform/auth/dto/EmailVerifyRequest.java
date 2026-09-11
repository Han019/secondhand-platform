package com.secondhand.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record EmailVerifyRequest(
        @NotBlank
        String email,

        @NotBlank
        String verificationCode
) {
}
