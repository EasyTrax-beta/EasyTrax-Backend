package com.easytrax.easytraxbackend.global.oauth.domain;

import com.easytrax.easytraxbackend.global.oauth.domain.info.KakaoUserInfo;
import com.easytrax.easytraxbackend.global.oauth.domain.info.UserInfo;
import com.easytrax.easytraxbackend.user.domain.RoleType;
import com.easytrax.easytraxbackend.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class IdTokenAttributes {

    private UserInfo userInfo;
    private SocialProvider socialProvider;

    public IdTokenAttributes(Map<String, Object> attributes, SocialProvider socialProvider){
        this.socialProvider = socialProvider;
        if(socialProvider == SocialProvider.KAKAO) this.userInfo = new KakaoUserInfo(attributes);
    }

    public User toUser() {
        return User.builder()
                .socialProvider(socialProvider)
                .roleType(RoleType.USER)
                .oauthId(userInfo.getId())
                .nickname(userInfo.getNickname())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .email(userInfo.getEmail())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }
}
