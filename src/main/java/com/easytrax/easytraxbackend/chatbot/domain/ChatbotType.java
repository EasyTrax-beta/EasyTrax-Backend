package com.easytrax.easytraxbackend.chatbot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatbotType {
    COMPLIANCE_ANALYSIS("통관거부사례 분석", "통관 거부 사례, 거부 사유, 해결 방법 등에 대해 질문해보세요."),
    REGULATION_INQUIRY("규제정보 조회", "수출 규정, 라벨링 요구사항, 인증 절차, 검역 규정에 대해 질문해보세요.");

    private final String displayName;
    private final String description;
}