package com.secondhand.platform.auth;

import com.secondhand.platform.auth.dto.LoginRequest;
import com.secondhand.platform.auth.dto.LoginResponse;
import com.secondhand.platform.auth.dto.SignupRequest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    //회원가입
    @Operation(
            summary = "회원가입",
            description = "회원가입하는 사용자의 정보를 db에 추가합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequest signupRequest
    ) {
        authService.signUp(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //로그인
    @Operation(
            summary = "로그인",
            description = "사용자의 아이디,패스워드를 받아서 db의 데이터와 일치하면 access token, refresh token을 발행합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
           @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
    //로그아웃
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자의 Refresh Token을 삭제합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Long userId
    ){
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/email/send")
    public void sendEmail() {

    }
    @PostMapping("/email/verify")
    public void verifyEmail() {

    }
}
