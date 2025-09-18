package com.easytrax.easytraxbackend.project.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Country {
    CHINA("중국", "CN"),
    USA("미국", "US"),
    JAPAN("일본", "JP"),
    GERMANY("독일", "DE"),
    FRANCE("프랑스", "FR"),
    UK("영국", "GB"),
    ITALY("이탈리아", "IT"),
    SPAIN("스페인", "ES"),
    NETHERLANDS("네덜란드", "NL"),
    BELGIUM("벨기에", "BE"),
    CANADA("캐나다", "CA"),
    AUSTRALIA("호주", "AU"),
    SINGAPORE("싱가포르", "SG"),
    HONG_KONG("홍콩", "HK"),
    THAILAND("태국", "TH"),
    VIETNAM("베트남", "VN"),
    INDIA("인도", "IN"),
    BRAZIL("브라질", "BR"),
    MEXICO("멕시코", "MX"),
    RUSSIA("러시아", "RU");

    private final String koreanName;
    private final String countryCode;
}