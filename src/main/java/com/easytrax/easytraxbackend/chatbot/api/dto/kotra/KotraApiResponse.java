package com.easytrax.easytraxbackend.chatbot.api.dto.kotra;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KotraApiResponse<T> {
    
    @JsonProperty("response")
    private Response<T> response;
    
    @Data
    public static class Response<T> {
        @JsonProperty("header")
        private Header header;
        
        @JsonProperty("body")
        private Body<T> body;
    }
    
    @Data
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;
        
        @JsonProperty("resultMsg")
        private String resultMsg;
    }
    
    @Data
    public static class Body<T> {
        @JsonProperty("itemList")
        private ItemList<T> itemList;
        
        @JsonProperty("numOfRows")
        private Integer numOfRows;
        
        @JsonProperty("pageNo")
        private Integer pageNo;
        
        @JsonProperty("totalCount")
        private Integer totalCount;
    }
    
    @Data
    public static class ItemList<T> {
        @JsonProperty("item")
        private Item<T> item;
    }
    
    @Data
    public static class Item<T> {
        @JsonProperty("korCompList")
        private KorCompList<T> korCompList;
    }
    
    @Data
    public static class KorCompList<T> {
        @JsonProperty("korComp")
        private List<T> korComp;
    }
}