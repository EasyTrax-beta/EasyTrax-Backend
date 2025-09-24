package com.easytrax.easytraxbackend.chatbot.api.dto.kotra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KotraCountryInfo {
    
    // 기본 국가 정보
    @JsonProperty("cntryIsoWd2CntCd")
    private String countryCode;
    
    @JsonProperty("cntryEngNm")
    private String countryNameEng;
    
    @JsonProperty("cntryKorNm") 
    private String countryNameKor;
    
    // 경제 정보
    @JsonProperty("gdpScr")
    private String gdp;
    
    @JsonProperty("exprtnScr")
    private String exportScale;
    
    @JsonProperty("imprtnScr")
    private String importScale;
    
    // 무역 및 투자 정보
    @JsonProperty("tradInvtsmtNationMainCtgryNm")
    private String tradeInvestmentCategory;
    
    @JsonProperty("tradInvtsmtNationSubCtgryNm")
    private String tradeInvestmentSubCategory;
    
    @JsonProperty("tradInvtsmtCtgryEtcCn")
    private String tradeInvestmentEtc;
    
    // 시장 정보
    @JsonProperty("mktCharMainCtgryNm")
    private String marketCharacteristics;
    
    @JsonProperty("mktCharSubCtgryNm")
    private String marketCharacteristicsDetail;
    
    @JsonProperty("mktCharEtcCn")
    private String marketCharacteristicsEtc;
    
    // 비즈니스 환경
    @JsonProperty("bsnsEnvrntMainCtgryNm")
    private String businessEnvironment;
    
    @JsonProperty("bsnsEnvrntSubCtgryNm")
    private String businessEnvironmentDetail;
    
    @JsonProperty("bsnsEnvrntEtcCn")
    private String businessEnvironmentEtc;
    
    // 현지 한국 기업 정보
    @JsonProperty("locKorpCmpnyMainCtgryNm")
    private String localKoreanCompanies;
    
    @JsonProperty("locKorpCmpnySubCtgryNm")
    private String localKoreanCompaniesDetail;
    
    @JsonProperty("locKorpCmpnyEtcCn")
    private String localKoreanCompaniesEtc;
}