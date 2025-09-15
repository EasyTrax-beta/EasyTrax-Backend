package com.easytrax.easytraxbackend.global.oauth.domain.info;

import java.util.Map;
import java.util.Optional;

public abstract class UserInfo {

    protected final Map<String, Object> attributes;

    public UserInfo(Map<String, Object> attributes){
        this.attributes = Map.copyOf(attributes);
    }

    protected Map<String, Object> rawAttributes() { 
        return attributes; 
    }

    public abstract String getId();
    public abstract Optional<String> getEmail();
    public abstract String getNickname();
    public abstract String getProfileImageUrl();
}
