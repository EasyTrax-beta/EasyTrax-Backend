package com.easytrax.easytraxbackend.chatbot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    USER("사용자"),
    ASSISTANT("어시스턴트");

    private final String displayName;
}