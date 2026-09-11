package com.secondhand.platform.auth.dto;

public record EmailSendRequest(
        String email,
        String verificationCode
) {
}
