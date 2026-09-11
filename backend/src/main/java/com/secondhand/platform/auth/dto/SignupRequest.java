package com.secondhand.platform.auth.dto;


public record SignupRequest(
    /*
    *   회원가입 요청
    * */
        String loginId,
        String password,
        String email,
        String nickname
) {
}
