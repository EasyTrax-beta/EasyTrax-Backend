package com.easytrax.easytraxbackend.global.oauth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    KAKAO("kakao");

    private final String registrationId;
}
