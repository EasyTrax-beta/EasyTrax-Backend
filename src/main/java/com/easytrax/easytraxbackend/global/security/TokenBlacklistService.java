package com.easytrax.easytraxbackend.global.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public void addToBlacklist(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Date expiresAt = decodedJWT.getExpiresAt();

            if (expiresAt != null) {
                long ttl = expiresAt.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    String key = BLACKLIST_PREFIX + hashToken(token);
                    // setIfAbsent 사용으로 중복 등록 시 TTL 재설정 방지
                    boolean wasSet =
                            redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofMillis(ttl));
                    if (wasSet) {
                        log.info("토큰이 블랙리스트에 추가되었습니다. TTL: {}ms", ttl);
                    }
                }
            }
        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.getMessage());
        }
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        redisTemplate.delete(key);
    }
}
