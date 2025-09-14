package com.easytrax.easytraxbackend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder kakaoJwtDecoder(
            @Value("${jwt.kakao.issuer:https://kauth.kakao.com}") String issuer,
            @Value("${jwt.kakao.client-id:}") String kakaoClientId
    ) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = (kakaoClientId == null || kakaoClientId.isBlank()) ? withIssuer : new DelegatingOAuth2TokenValidator<>(
                        withIssuer, new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(kakaoClientId)));
        decoder.setJwtValidator(withAudience);
        return decoder;
    }
}
