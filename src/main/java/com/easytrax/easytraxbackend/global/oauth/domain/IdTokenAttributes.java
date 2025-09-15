package com.easytrax.easytraxbackend.global.oauth.domain;

import com.easytrax.easytraxbackend.global.oauth.domain.info.KakaoUserInfo;
import com.easytrax.easytraxbackend.global.oauth.domain.info.UserInfo;
import com.easytrax.easytraxbackend.user.domain.RoleType;
import com.easytrax.easytraxbackend.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Getter
public class IdTokenAttributes {

    private final UserInfo userInfo;
    private final SocialProvider socialProvider;

    public IdTokenAttributes(Map<String, Object> attributes, SocialProvider socialProvider){
        this.socialProvider = Objects.requireNonNull(socialProvider, "socialProvider");
        Objects.requireNonNull(attributes, "attributes");
        switch (this.socialProvider) {
            case KAKAO -> this.userInfo = new KakaoUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported social provider: " + this.socialProvider);
        }
    }

    public User toUser() {
        String email = userInfo.getEmail().orElse(null);
        return User.builder()
                .socialProvider(socialProvider)
                .roleType(RoleType.USER)
                .oauthId(userInfo.getId())
                .nickname(userInfo.getNickname())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .email(email)
                .lastLoginAt(LocalDateTime.now())
                .build();
    }
}
