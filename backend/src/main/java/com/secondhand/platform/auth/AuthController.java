package com.secondhand.platform.auth;

import com.secondhand.platform.auth.dto.*;

import com.secondhand.platform.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 및 인증 API")
public class AuthController {

    private final AuthService authService;
    //회원가입
    @Operation(
            summary = "회원가입",
            description = "로그인 ID, 이메일, 닉네임의 중복 여부를 확인하고 비밀번호를 암호화하여 사용자를 등록합니다."
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
            description = "로그인 ID와 비밀번호를 확인한 뒤 Access Token과 Refresh Token을 발급합니다."
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
            description = "현재 사용자의 저장된 Refresh Token을 삭제합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Long userId
    ){
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }
    @Operation(
            summary = "Access Token 재발급",
            description = "유효한 Refresh Token을 확인하고 새로운 Access Token을 발급합니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
        @Valid @RequestBody RefreshRequest request
    ){
        RefreshResponse response = authService.refresh(request.refreshToken());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그인 ID 중복 확인",
            description = "로그인 ID를 사용할 수 있으면 isAvailable=true, 이미 사용 중이면 false를 반환합니다."
    )
    @GetMapping("/check/login-id")
    public ResponseEntity<AvailabilityResponse> checkLoginId(@RequestParam("loginId") String loginId){
        return ResponseEntity.ok(new AvailabilityResponse(authService.checkLoginId(loginId)));
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = "닉네임을 사용할 수 있으면 isAvailable=true, 이미 사용 중이면 false를 반환합니다."
    )
    @GetMapping("/check/nickname")
    public ResponseEntity<AvailabilityResponse> checkNickname(@RequestParam("nickname") String nickname){
        return ResponseEntity.ok(new AvailabilityResponse(authService.checkNickname(nickname)));
    }


    @Operation(
            summary = "인증 이메일 발송 (미구현)",
            description = "현재 이메일 발송 로직이 없어 요청에 응답만 반환합니다."
    )
    @PostMapping("/email/send")
    public void sendEmail() {

    }
    @Operation(
            summary = "이메일 인증 (미구현)",
            description = "현재 인증 로직이 없어 요청에 응답만 반환합니다."
    )
    @PostMapping("/email/verify")
    public void verifyEmail() {

    }
}
