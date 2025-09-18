package com.easytrax.easytraxbackend.hscode.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum FoodHSCode {
    // 라면/면류
    INSTANT_NOODLES("190230", "라면", "Instant noodles", 
                   List.of("라면", "인스턴트", "신라면", "짜파게티", "컵라면", "봉지라면", "즉석면"), 
                   "밀가루, 야자유, 조미료", "즉석식품"),
    
    PASTA("190211", "파스타", "Pasta containing eggs", 
          List.of("파스타", "스파게티", "마카로니", "면"), 
          "밀가루, 달걀", "면류"),
    
    // 과자/스낵류
    BISCUITS("190531", "비스킷", "Sweet biscuits", 
             List.of("비스킷", "쿠키", "과자", "크래커"), 
             "밀가루, 설탕, 버터", "과자류"),
    
    CHOCOLATE("180632", "초콜릿", "Chocolate confectionery", 
              List.of("초콜릿", "초콜렛", "쇼콜라", "카카오"), 
              "카카오, 설탕, 우유", "과자류"),
    
    POTATO_CHIPS("200410", "감자칩", "Potato chips", 
                 List.of("감자칩", "포테토칩", "칩", "스낵"), 
                 "감자, 식용유, 소금", "스낵류"),
    
    // 음료류
    COFFEE("210111", "인스턴트 커피", "Instant coffee", 
           List.of("커피", "인스턴트커피", "원두", "카페"), 
           "커피원두, 설탕", "음료"),
    
    GREEN_TEA("090230", "녹차", "Green tea", 
              List.of("녹차", "차", "티", "잎차"), 
              "차잎", "음료"),
    
    CARBONATED_DRINKS("220210", "탄산음료", "Carbonated soft drinks", 
                      List.of("콜라", "사이다", "탄산음료", "음료수"), 
                      "물, 설탕, 탄산", "음료"),
    
    // 소스/조미료류
    SOY_SAUCE("210310", "간장", "Soy sauce", 
              List.of("간장", "소이소스", "조선간장"), 
              "대두, 소금", "조미료"),
    
    GOCHUJANG("210390", "고추장", "Korean chili paste", 
              List.of("고추장", "칠리페이스트", "매운장"), 
              "고추, 콩, 쌀", "조미료"),
    
    KIMCHI("200599", "김치", "Kimchi", 
           List.of("김치", "배추김치", "발효식품"), 
           "배추, 고춧가루, 마늘", "발효식품"),
    
    // 유제품류
    MILK("040110", "우유", "Fresh milk", 
         List.of("우유", "밀크", "신선우유"), 
         "우유", "유제품"),
    
    CHEESE("040610", "치즈", "Fresh cheese", 
           List.of("치즈", "체다치즈", "모짜렐라"), 
           "우유, 유산균", "유제품"),
    
    // 육류/수산물 가공품
    CANNED_TUNA("160414", "참치통조림", "Canned tuna", 
                List.of("참치", "튜나", "통조림", "캔"), 
                "참치, 기름", "수산가공품"),
    
    DRIED_SEAWEED("121220", "김", "Dried seaweed", 
                  List.of("김", "해조류", "마른김"), 
                  "김", "해조류"),
    
    // 견과류
    ALMONDS("080211", "아몬드", "Almonds", 
            List.of("아몬드", "견과류"), 
            "아몬드", "견과류"),
    
    PEANUTS("120241", "땅콩", "Peanuts", 
            List.of("땅콩", "피넛"), 
            "땅콩", "견과류");

    private final String hsCode;
    private final String koreanName;
    private final String englishName;
    private final List<String> keywords;
    private final String materialInfo;
    private final String category;

    public static List<FoodHSCode> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        // 정확한 매칭 우선 (제품명과 정확히 일치)
        List<FoodHSCode> exactMatches = Arrays.stream(FoodHSCode.values())
                .filter(food -> food.keywords.stream()
                        .anyMatch(k -> k.toLowerCase().equals(lowerKeyword)) ||
                        food.koreanName.toLowerCase().equals(lowerKeyword))
                .collect(Collectors.toList());
        
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        
        // 부분 매칭 (포함 관계)
        return Arrays.stream(FoodHSCode.values())
                .filter(food -> food.keywords.stream()
                        .anyMatch(k -> k.toLowerCase().contains(lowerKeyword) || 
                                      lowerKeyword.contains(k.toLowerCase())) ||
                        food.koreanName.toLowerCase().contains(lowerKeyword) ||
                        food.englishName.toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public static List<FoodHSCode> findByMaterial(String material) {
        if (material == null || material.trim().isEmpty()) {
            return List.of();
        }
        
        String lowerMaterial = material.toLowerCase();
        return Arrays.stream(FoodHSCode.values())
                .filter(food -> food.materialInfo.toLowerCase().contains(lowerMaterial))
                .collect(Collectors.toList());
    }

    public static FoodHSCode findByHsCode(String hsCode) {
        return Arrays.stream(FoodHSCode.values())
                .filter(food -> food.hsCode.equals(hsCode))
                .findFirst()
                .orElse(null);
    }
}