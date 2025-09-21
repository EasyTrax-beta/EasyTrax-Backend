package com.easytrax.easytraxbackend.nutritionlabel.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LabelFormat {
    USA_FDA("미국 FDA 포맷"),
    CHINA_GB28050("중국 GB 28050 포맷"),
    JAPAN_JAS("일본 JAS 포맷"),
    EU_1169("유럽연합 EU 1169/2011 포맷");

    private final String description;
}