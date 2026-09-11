package com.secondhand.platform.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenStore {
    private static final String REFRESH_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    //refresh token 저장
    public void saveRefreshToken(Long userId, String refreshToken, Duration ttl){
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + userId,
                refreshToken,
                ttl
        );
    }

    //redis에 저장된 refresh token 비교
    public boolean matchRefreshToken(String userId,String refreshToken){

        return redisTemplate.opsForValue().get(REFRESH_PREFIX + userId).equals(refreshToken);
    }

    //로그아웃 시, Refresh Token 삭제
    public void removeRefreshToken(Long userId){
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }
}
