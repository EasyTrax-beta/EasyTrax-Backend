package com.easytrax.easytraxbackend.global.oauth.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.global.oauth.api.dto.response.LoginResponse;
import com.easytrax.easytraxbackend.global.oauth.api.dto.response.UserInfoResponse;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import com.easytrax.easytraxbackend.global.security.JwtService;
import com.easytrax.easytraxbackend.global.security.TokenBlacklistService;
import com.easytrax.easytraxbackend.user.domain.User;
import com.easytrax.easytraxbackend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final IdTokenService idTokenService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public LoginResponse login(String idToken) {
        CustomUserDetails userDetails = idTokenService.loadUserByAccessToken(idToken);
        String email = userDetails.getUsername();
        Long userId = userDetails.getUserId();

        String accessToken = jwtService.createAccessToken(email, userId);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.updateRefreshToken(email, refreshToken);
        return new LoginResponse(accessToken, refreshToken);
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        return new UserInfoResponse(
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRoleType()
        );
    }

    @Transactional
    public LoginResponse reissueTokens(String oldAccessToken, String oldRefreshToken) {
        if (!jwtService.isTokenValid(oldRefreshToken)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        User user = userRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new GeneralException(ErrorStatus.EXPIRED_TOKEN));

        // AccessToken과 사용자 일치 검증 (만료 허용)
        Long userIdFromAccess = jwtService.getUserIdAllowExpired(oldAccessToken);
        if (!user.getId().equals(userIdFromAccess)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        String newAccessToken = jwtService.createAccessToken(user.getEmail(), user.getId());
        String newRefreshToken = jwtService.createRefreshToken();

        user.updateRefreshToken(newRefreshToken);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String accessToken) {
        String email = jwtService.verifyTokenAndGetEmail(accessToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 액세스 토큰을 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(accessToken);

        // 리프레시 토큰 무효화
        user.updateRefreshToken(null);
    }
}
