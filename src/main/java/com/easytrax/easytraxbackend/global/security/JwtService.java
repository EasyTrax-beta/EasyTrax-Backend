package com.easytrax.easytraxbackend.global.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.user.domain.User;
import com.easytrax.easytraxbackend.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String USERID_CLAIM = "userId";
    private static final String BEARER_PREFIX = "Bearer ";

    public String createAccessToken(String email, Long userId) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                .withClaim(EMAIL_CLAIM, email)
                .withClaim(USERID_CLAIM, userId)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, BEARER_PREFIX + accessToken);
    }

    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, BEARER_PREFIX + refreshToken);
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
    }

    public Optional<String> extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return Optional.of(bearerToken.replace(BEARER_PREFIX, ""));
        }
        return Optional.empty();
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .flatMap(this::extractToken);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .flatMap(this::extractToken);
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            String email = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .build()
                    .verify(accessToken)
                    .getClaim(EMAIL_CLAIM)
                    .asString();
            return (email == null || email.isBlank()) ? Optional.empty() : Optional.of(email);
        } catch (JWTVerificationException e) {
            log.warn("유효하지 않은 Access Token 입니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Long> extractUserId(String accessToken) {
        try {
            Long userId = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .build()
                    .verify(accessToken)
                    .getClaim(USERID_CLAIM)
                    .asLong();
            return Optional.ofNullable(userId);
        } catch (JWTVerificationException e) {
            log.warn("유효하지 않은 Access Token 입니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        String hashedRefreshToken = refreshToken != null ? hashRefreshToken(refreshToken) : null;
        user.updateRefreshToken(hashedRefreshToken);
    }

    private String hashRefreshToken(String refreshToken) {
        // SHA-256으로 먼저 해시하여 길이를 고정 (64 hex chars < 72 bytes)
        String sha256Hash = getSha256Hash(refreshToken);
        // SHA-256 결과를 BCrypt로 해시
        return passwordEncoder.encode(sha256Hash);
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.warn("유효하지 않은 토큰입니다. 원인: {}", e.getMessage());
            return false;
        }
    }

    public String verifyTokenAndGetEmail(String token) {
        try {
            String email = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .build()
                    .verify(token)
                    .getClaim(EMAIL_CLAIM)
                    .asString();
            if (email == null || email.isBlank()) {
                throw new GeneralException(ErrorStatus.INVALID_TOKEN);
            }
            return email;
        } catch (TokenExpiredException e) {
            log.warn("만료된 토큰입니다. {}", e.getMessage());
            throw new GeneralException(ErrorStatus.EXPIRED_TOKEN);
        } catch (JWTVerificationException e) {
            log.warn("유효하지 않은 토큰입니다. {}", e.getMessage());
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
    }

    public Long verifyTokenAndGetUserId(String token) {
        try {
            Long userId = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .build()
                    .verify(token)
                    .getClaim(USERID_CLAIM)
                    .asLong();
            if (userId == null) {
                throw new GeneralException(ErrorStatus.INVALID_TOKEN);
            }
            return userId;
        } catch (TokenExpiredException e) {
            log.warn("만료된 토큰입니다. {}", e.getMessage());
            throw new GeneralException(ErrorStatus.EXPIRED_TOKEN);
        } catch (JWTVerificationException e) {
            log.warn("유효하지 않은 토큰입니다. {}", e.getMessage());
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
    }

    public Optional<User> findUserByRefreshToken(String refreshToken) {
        String sha256Hash = getSha256Hash(refreshToken);
        return userRepository.findByRefreshTokenIsNotNull()
                .stream()
                .filter(user -> passwordEncoder.matches(sha256Hash, user.getRefreshToken()))
                .findFirst();
    }

    private String getSha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
