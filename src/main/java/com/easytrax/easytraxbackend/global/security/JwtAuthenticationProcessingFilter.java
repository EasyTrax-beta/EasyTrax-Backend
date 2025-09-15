package com.easytrax.easytraxbackend.global.security;

import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.user.domain.User;
import com.easytrax.easytraxbackend.user.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return;
        }

        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail(), user.getId()),
                            reIssuedRefreshToken);
                });
    }

    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  FilterChain filterChain) throws
            ServletException, IOException {

        jwtService.extractAccessToken(request).ifPresent(accessToken -> {
            try {
                // 블랙리스트 검증 추가
                if (!jwtService.isTokenValid(accessToken)) {
                    log.debug("블랙리스트에 포함된 토큰이거나 유효하지 않은 토큰입니다.");
                    return;
                }

                String email = jwtService.verifyTokenAndGetEmail(accessToken);
                userRepository.findByEmail(email).ifPresent(user -> {
                    log.info("JWT 인증 성공 - userId: {}", user.getId());
                    saveAuthentication(user);
                });
            } catch (GeneralException ex) {
                log.debug("AccessToken 인증 실패: {}", ex.getMessage());
            }
        });

        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(User user) {
        CustomUserDetails userDetailsUser = new CustomUserDetails(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleType().getKey())),
                user.getEmail(),
                user.getRoleType(),
                user.getId());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
