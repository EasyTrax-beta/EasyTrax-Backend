package com.easytrax.easytraxbackend.chatbot.api.dto.kotra;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UsaGlobalIssueItem {
    
    @JsonProperty("nat")
    private String country;
    
    @JsonProperty("regDt")
    private String registrationDate;
    
    @JsonProperty("nttSj")
    private String title;
    
    @JsonProperty("fileLink")
    private String fileLink;
    
    @JsonProperty("nttCtgryNm")
    private String category;
    
    @JsonProperty("smmarCn")
    private String summary;
    
    @JsonProperty("othbcDt")
    private String publicationDate;
    
    @JsonProperty("kbc")
    private String tradeOffice;
    
    @JsonProperty("regn")
    private String region;
}