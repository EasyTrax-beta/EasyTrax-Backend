package com.easytrax.easytraxbackend.invoice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceFormat {
    USA_STANDARD("미국 표준 상업송장"),
    CHINA_STANDARD("중국 표준 상업송장 (商业发票)"),
    JAPAN_STANDARD("일본 표준 상업송장 (商業インボイス)"),
    EU_STANDARD("유럽연합 표준 상업송장");

    private final String description;
}