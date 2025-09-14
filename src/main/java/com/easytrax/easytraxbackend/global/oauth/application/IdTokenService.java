package com.easytrax.easytraxbackend.global.oauth.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.global.oauth.domain.IdTokenAttributes;
import com.easytrax.easytraxbackend.global.oauth.domain.SocialProvider;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import com.easytrax.easytraxbackend.user.domain.User;
import com.easytrax.easytraxbackend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdTokenService {

    private final JwtDecoder kakaoJwtDecoder;
    private final UserRepository userRepository;

    public CustomUserDetails loadUserByAccessToken(String idToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(idToken);
            SocialProvider socialProvider = checkIssuer(decodedJWT.getIssuer());

            Map<String, Object> attributes = tokenToattributes(idToken, socialProvider);
            IdTokenAttributes idTokenAttributes = new IdTokenAttributes(attributes, socialProvider);

            User findUser = checkUser(idTokenAttributes);

            return new CustomUserDetails(
                    Collections.singleton(new SimpleGrantedAuthority(findUser.getRoleType().getKey())),
                    findUser.getEmail(),
                    findUser.getRoleType(),
                    findUser.getId()
            );
        } catch (JWTDecodeException | JwtException e) {
            log.warn("ID 토큰 인증 오류: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
    }

    private SocialProvider checkIssuer(String issuer) {
        if ("https://kauth.kakao.com".equals(issuer)) return SocialProvider.KAKAO;
        throw new GeneralException(ErrorStatus.INVALID_TOKEN);
    }

    private User checkUser(IdTokenAttributes attrs) {
        return userRepository
                .findBySocialProviderAndOauthId(attrs.getSocialProvider(), attrs.getUserInfo().getId())
                .map(u -> { u.markLogin(); return u; })
                .orElseGet(() -> createUser(attrs));
    }

    private User createUser(IdTokenAttributes idTokenAttributes) {
        User createdUser = idTokenAttributes.toUser();
        return userRepository.save(createdUser);
    }

    private Map<String, Object> tokenToattributes(String idToken, SocialProvider socialProvider) {
        try {
            if (socialProvider == SocialProvider.KAKAO) return kakaoJwtDecoder.decode(idToken).getClaims();
        } catch (JwtException e) {
            log.warn("ID 토큰 검증 실패 ({}): {}", socialProvider, e.getMessage());
            throw e;
        }
        return null;
    }
}
