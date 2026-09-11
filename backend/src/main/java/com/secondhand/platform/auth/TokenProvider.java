package com.secondhand.platform.auth;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;


@Component
//@RequiredArgsConstructor
public class TokenProvider {

    //access token 생성, refresh token 생성
    //jwt 검증
    //jwt 내용 추출
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final String secretKey;
    private final Duration accessTokenExpireTime;
    @Getter
    private final Duration refreshTokenExpireTime;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}") Duration accessTokenExpireTime,
            @Value("${jwt.refresh-token-expire-time}") Duration refreshTokenExpireTime
    ){
        this.secretKey = secretKey;
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }
    //jwt 비밀키
    private SecretKey key(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    //토큰 발행
    public String issueAccessToken(Long userId, String role){
        return build(userId,TYPE_ACCESS,accessTokenExpireTime.toMillis()).claim("role", role).compact();
    }

    public String issueRefreshToken(Long userId){
        return build(userId,TYPE_REFRESH,refreshTokenExpireTime.toMillis()).compact();
    }

    //jwt 토큰 발행
    private JwtBuilder build(
            Long userId,
            String type,
            long expireTime
    ) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireTime))
                .signWith(key());
    }

}
