package com.secondhand.platform.auth;


import com.secondhand.platform.auth.dto.LoginRequest;
import com.secondhand.platform.auth.dto.LoginResponse;
import com.secondhand.platform.auth.dto.SignupRequest;
import com.secondhand.platform.common.exception.DuplicatedUserException;
import com.secondhand.platform.common.exception.GlobalExceptionHandler;
import com.secondhand.platform.common.exception.LoginFailedException;
import com.secondhand.platform.user.User;
import com.secondhand.platform.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final TokenStore tokenStore;
    //로그인
    public LoginResponse login(LoginRequest loginRequest){
        User user = userRepository.findByLoginId(loginRequest.loginId()).orElseThrow(
                ()-> new LoginFailedException("존재하지 않는 사용자입니다."));

        boolean matches = passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()
        );

        if (!matches){
            throw new LoginFailedException("비밀번호가 일치하지 않습니다.");
        }

        // 이후 로그인 성공
        // JWT 토큰 발행
        //Access Token 발급
        String accessToken = tokenProvider.issueAccessToken(user.getId(), user.getRole().name());
        //Refresh Token 발급
        String refreshToken = tokenProvider.issueRefreshToken(user.getId());
        //saveRefresh
        tokenStore.saveRefreshToken(user.getId(),refreshToken,tokenProvider.getRefreshTokenExpireTime());

        //클라이언트에 토큰 반환
        return new LoginResponse(accessToken,refreshToken);
    }
    //회원가입
    @Transactional
    public void signUp(SignupRequest signupRequest){

        //이미 있는 이메일인지, 이미 있는 닉네임인지, 이미 있는 loginId인지
        //이미 있는 이메일이라면
        if(userRepository.existsByEmail(signupRequest.email())){
            throw new DuplicatedUserException("이미 가입된 이메일입니다.");
        }
        //이미 있는 닉네임인지
        if(userRepository.existsByNickname(signupRequest.nickname())){
            throw new DuplicatedUserException("이미 사용중인 닉네임입니다.");
        }
        if (userRepository.existsByLoginId(signupRequest.loginId())){
            throw new DuplicatedUserException("이미 사용중인 id입니다.");
        }
        //db에다 저장해야함
        //비번은 암호화해서
        String password = passwordEncoder.encode(signupRequest.password());

        //엔티티 객체 만들어서 유저 레포지토리에 저장
        User user = User.create(
                signupRequest.loginId(),
                password,
                signupRequest.email(),
                signupRequest.nickname()
        );
        userRepository.save(user);

    }

    //로그아웃
    public void logout(Long userId){
        //access token에서 userId 확인후 삭제
        tokenStore.removeRefreshToken(userId);
    }


}
