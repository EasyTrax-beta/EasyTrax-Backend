package com.easytrax.easytraxbackend.global.oauth.api;

import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.oauth.api.dto.response.LoginResponse;
import com.easytrax.easytraxbackend.global.oauth.api.dto.response.UserInfoResponse;
import com.easytrax.easytraxbackend.global.oauth.application.AuthService;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "로그인 API", description = "소셜 로그인 및 토큰 재발급")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "OIDC 로그인", description = "구글/카카오에서 받은 id_token으로 로그인/회원가입 처리 후 서비스의 토큰을 발급합니다. " +
            "issuer 필드를 통해 자동으로 소셜 제공자를 판단합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestHeader("id_token") String idToken) {
        LoginResponse tokens = authService.login(idToken);
        return ResponseEntity.ok(ApiResponse.onSuccess(tokens));
    }

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다. (AccessToken 필요)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>>getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null));
        }
        UserInfoResponse userInfo = authService.getUserInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(userInfo));
    }

    @Operation(summary = "Access/Refresh 토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access/Refresh 토큰 쌍을 발급받습니다. " +
            "DB의 Refresh Token은 새로 발급된 토큰으로 업데이트됩니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissueTokens(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Parameter(description = "Refresh Token (Bearer 스키마 포함/제외 모두 허용)", required = true)
            @RequestHeader("RefreshToken") String refreshToken) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.onFailure("AUTH401", "유효하지 않은 Authorization 헤더입니다.", null));
        }
        String accessToken = authorizationHeader.substring(7);  // "Bearer " 제거
        String normalizedRefresh = refreshToken != null && refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        LoginResponse newTokens = authService.reissueTokens(accessToken, normalizedRefresh);
        return ResponseEntity.ok(ApiResponse.onSuccess(newTokens));
    }

    @Operation(summary = "로그아웃", description = "DB의 Refresh Token을 null로 변경하여 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestHeader("RefreshToken") String refreshToken) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.onFailure("AUTH401", "유효하지 않은 Authorization 헤더입니다.", null));
        }
        String accessToken = authorizationHeader.substring(7);
        String normalizedRefresh = refreshToken != null && refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        authService.logout(accessToken, normalizedRefresh);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
