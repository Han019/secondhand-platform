package com.secondhand.platform.auth.jwt;

import com.secondhand.platform.common.exception.InvalidTokenException;
import com.secondhand.platform.common.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
//@RequiredArgsConstructor
public class JwtTokenProvider {

    //access token 생성, refresh token 생성
    //jwt 검증
    //jwt 내용 추출

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTokenExpireTime;
    private final Duration refreshTokenExpireTime;

    public JwtTokenProvider(JwtProperties jwtProperties){
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireTime = jwtProperties.accessTokenExpireTime();
        this.refreshTokenExpireTime = jwtProperties.refreshTokenExpireTime();
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
                .signWith(key);
    }

    private Claims parse(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    // 토큰 검증
    public void validateToken(String token){
        try {
            parse(token);

        }catch(ExpiredJwtException e){
            //access Token 만료
            log.info("Expired JWT Token: {} ",e.getMessage());
            throw new TokenExpiredException("토큰이 만료되었습니다.");

        } catch(JwtException|IllegalArgumentException e){
            log.warn("Invalid JWT Token: {} ",e.getMessage());
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

    public boolean isAccessToken(String token){
        return TYPE_ACCESS.equals(parse(token).get("type", String.class));
    }
    public boolean isRefreshToken(String token){
        return TYPE_REFRESH.equals(parse(token).get("type", String.class));
    }

    public Long getUserId(String token){
        return Long.valueOf(parse(token).getSubject());
    }
    public String getRole(String token){
        return parse(token).get("role", String.class);
    }
}
