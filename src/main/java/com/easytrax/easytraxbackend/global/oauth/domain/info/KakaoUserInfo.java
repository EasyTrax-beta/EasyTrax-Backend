package com.easytrax.easytraxbackend.global.oauth.domain.info;

import java.util.Map;
import java.util.Optional;

public class KakaoUserInfo extends UserInfo {

    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("sub"));
    }

    @Override
    public Optional<String> getEmail() {
        return Optional.ofNullable((String) attributes.get("email"));
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("picture");
    }
}
