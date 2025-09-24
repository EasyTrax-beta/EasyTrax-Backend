package com.easytrax.easytraxbackend.chatbot.application;

import com.easytrax.easytraxbackend.chatbot.api.dto.kotra.KotraApiResponse;
import com.easytrax.easytraxbackend.chatbot.api.dto.kotra.KotraCountryInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KotraApiService {

    private final WebClient webClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper;

    public KotraApiService(
            @Value("${kotra.api.key}") String serviceKey,
            @Value("${kotra.api.base-url}") String baseUrl,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public CompletableFuture<KotraCountryInfo> getCountryInformation(String countryCode) {
        log.debug("KOTRA API 요청 시작 - 국가코드: {}", countryCode);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 수동으로 URL 구성하여 인코딩 문제 해결
                String encodedServiceKey = serviceKey.replace("/", "%2F").replace("+", "%2B").replace("=", "%3D");
                String fullUrl = String.format("https://apis.data.go.kr/B410001/kotra_nationalInformation/natnInfo/natnInfo?serviceKey=%s&isoWd2CntCd=%s&type=json", 
                        encodedServiceKey, countryCode);
                
                log.debug("KOTRA API 요청 URI: {}", fullUrl);
                
                java.net.URL url = new java.net.URL(fullUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Java/17");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                
                int responseCode = conn.getResponseCode();
                log.debug("KOTRA API 응답 코드: {}", responseCode);
                
                if (responseCode == 200) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        String responseBody = response.toString();
                        log.debug("KOTRA API 원본 응답 길이: {} bytes", responseBody.length());
                        return parseCountryInfoResponse(responseBody);
                    }
                } else {
                    log.error("KOTRA API HTTP 오류 - 상태코드: {}", responseCode);
                    return new KotraCountryInfo();
                }
                
            } catch (Exception e) {
                log.error("KOTRA API 호출 중 오류 발생", e);
                return new KotraCountryInfo();
            }
        });
    }

    public CompletableFuture<String> searchRelevantKotraData(String userQuery) {
        String countryCode = extractCountryCodeFromQuery(userQuery);
        
        if (countryCode == null) {
            return CompletableFuture.completedFuture("");  // 빈 문자열 반환하여 AI가 자체 정보로 답변
        }
        
        return getCountryInformation(countryCode)
                .thenApply(this::formatKotraDataForAi);
    }

    private KotraCountryInfo parseCountryInfoResponse(String responseBody) {
        try {
            // 응답 내용 로깅 (디버깅용)
            log.debug("KOTRA API 전체 응답: {}", responseBody);
            
            // HTML 응답인지 확인
            if (responseBody.trim().startsWith("<")) {
                log.warn("KOTRA API가 HTML 응답을 반환했습니다. API 키나 URL을 확인하세요.");
                return new KotraCountryInfo();
            }
            
            // JSON 응답인지 확인
            if (!responseBody.trim().startsWith("{")) {
                log.warn("KOTRA API가 JSON이 아닌 응답을 반환했습니다: {}", responseBody);
                return new KotraCountryInfo();
            }
            
            KotraApiResponse<KotraCountryInfo> response = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<KotraApiResponse<KotraCountryInfo>>() {}
            );
            
            log.debug("파싱된 응답 객체: response={}", response);
            
            if (response.getResponse() != null) {
                log.debug("response.getResponse(): {}", response.getResponse());
                if (response.getResponse().getBody() != null) {
                    log.debug("response.getResponse().getBody(): {}", response.getResponse().getBody());
                    if (response.getResponse().getBody().getItemList() != null) {
                        log.debug("ItemList 존재함");
                        if (response.getResponse().getBody().getItemList().getItem() != null) {
                            log.debug("Item 존재함");
                            if (response.getResponse().getBody().getItemList().getItem().getKorCompList() != null) {
                                log.debug("KorCompList 존재함");
                                if (response.getResponse().getBody().getItemList().getItem().getKorCompList().getKorComp() != null &&
                                    !response.getResponse().getBody().getItemList().getItem().getKorCompList().getKorComp().isEmpty()) {
                                    KotraCountryInfo countryInfo = response.getResponse().getBody().getItemList().getItem().getKorCompList().getKorComp().get(0);
                                    log.debug("성공적으로 파싱된 국가정보: {}", countryInfo.getCountryNameKor());
                                    return countryInfo;
                                }
                            }
                        }
                    }
                }
            }
            
            log.warn("KOTRA API 응답에 예상된 데이터 구조가 없습니다");
            return new KotraCountryInfo();
        } catch (Exception e) {
            log.error("KOTRA API 응답 파싱 중 오류 발생. 응답 길이: {}, 오류: {}", 
                responseBody.length(), e.getMessage(), e);
            return new KotraCountryInfo();
        }
    }

    private String extractCountryCodeFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String lowerQuery = query.toLowerCase();
        
        // 수출 대상 국가만 지원: 미국, 중국, 일본, 유럽
        // 미국 관련 키워드
        if (lowerQuery.contains("미국") || lowerQuery.contains("usa") || lowerQuery.contains("united states") ||
            lowerQuery.contains("america") || lowerQuery.contains("american") || lowerQuery.contains("us ") ||
            lowerQuery.contains("달러") || lowerQuery.contains("dollar")) return "US";
            
        // 중국 관련 키워드  
        if (lowerQuery.contains("중국") || lowerQuery.contains("china") || lowerQuery.contains("chinese") ||
            lowerQuery.contains("위안") || lowerQuery.contains("yuan") || lowerQuery.contains("prc")) return "CN";
            
        // 일본 관련 키워드
        if (lowerQuery.contains("일본") || lowerQuery.contains("japan") || lowerQuery.contains("japanese") ||
            lowerQuery.contains("엔화") || lowerQuery.contains("yen") || lowerQuery.contains("도쿄") || lowerQuery.contains("tokyo")) return "JP";
        
        // 유럽 주요국
        if (lowerQuery.contains("독일") || lowerQuery.contains("germany") || lowerQuery.contains("german") ||
            lowerQuery.contains("베를린") || lowerQuery.contains("berlin")) return "DE";
        if (lowerQuery.contains("프랑스") || lowerQuery.contains("france") || lowerQuery.contains("french") ||
            lowerQuery.contains("파리") || lowerQuery.contains("paris")) return "FR";
        if (lowerQuery.contains("영국") || lowerQuery.contains("uk") || lowerQuery.contains("britain") ||
            lowerQuery.contains("british") || lowerQuery.contains("london") || lowerQuery.contains("런던") ||
            lowerQuery.contains("파운드") || lowerQuery.contains("pound")) return "GB";
        if (lowerQuery.contains("이탈리아") || lowerQuery.contains("italy") || lowerQuery.contains("italian") ||
            lowerQuery.contains("로마") || lowerQuery.contains("rome")) return "IT";
        if (lowerQuery.contains("스페인") || lowerQuery.contains("spain") || lowerQuery.contains("spanish") ||
            lowerQuery.contains("마드리드") || lowerQuery.contains("madrid")) return "ES";
        if (lowerQuery.contains("네덜란드") || lowerQuery.contains("netherlands") || lowerQuery.contains("dutch") ||
            lowerQuery.contains("암스테르담") || lowerQuery.contains("amsterdam")) return "NL";
        
        // 유럽 전체 언급 시 독일 대표로 사용
        if (lowerQuery.contains("유럽") || lowerQuery.contains("europe") || lowerQuery.contains("eu") ||
            lowerQuery.contains("유로") || lowerQuery.contains("euro")) return "DE";
        
        return null;
    }

    private String formatKotraDataForAi(KotraCountryInfo countryInfo) {
        if (countryInfo.getCountryNameKor() == null) {
            return "";  // 빈 문자열 반환하여 AI가 자체 정보로 답변
        }

        StringBuilder formattedData = new StringBuilder();
        formattedData.append("=== KOTRA 공식 국가정보 ===\n");
        formattedData.append("국가: ").append(countryInfo.getCountryNameKor())
                .append(" (").append(countryInfo.getCountryNameEng()).append(")\n");
        
        if (countryInfo.getGdp() != null) {
            formattedData.append("GDP: ").append(countryInfo.getGdp()).append("\n");
        }
        
        if (countryInfo.getExportScale() != null) {
            formattedData.append("수출 규모: ").append(countryInfo.getExportScale()).append("\n");
        }
        
        if (countryInfo.getImportScale() != null) {
            formattedData.append("수입 규모: ").append(countryInfo.getImportScale()).append("\n");
        }
        
        if (countryInfo.getTradeInvestmentCategory() != null) {
            formattedData.append("무역투자 분류: ").append(countryInfo.getTradeInvestmentCategory()).append("\n");
            if (countryInfo.getTradeInvestmentEtc() != null) {
                formattedData.append("  상세: ").append(countryInfo.getTradeInvestmentEtc()).append("\n");
            }
        }
        
        if (countryInfo.getMarketCharacteristics() != null) {
            formattedData.append("시장 특성: ").append(countryInfo.getMarketCharacteristics()).append("\n");
            if (countryInfo.getMarketCharacteristicsEtc() != null) {
                formattedData.append("  상세: ").append(countryInfo.getMarketCharacteristicsEtc()).append("\n");
            }
        }
        
        if (countryInfo.getBusinessEnvironment() != null) {
            formattedData.append("비즈니스 환경: ").append(countryInfo.getBusinessEnvironment()).append("\n");
            if (countryInfo.getBusinessEnvironmentEtc() != null) {
                formattedData.append("  상세: ").append(countryInfo.getBusinessEnvironmentEtc()).append("\n");
            }
        }
        
        if (countryInfo.getLocalKoreanCompanies() != null) {
            formattedData.append("현지 한국기업: ").append(countryInfo.getLocalKoreanCompanies()).append("\n");
            if (countryInfo.getLocalKoreanCompaniesEtc() != null) {
                formattedData.append("  상세: ").append(countryInfo.getLocalKoreanCompaniesEtc()).append("\n");
            }
        }
        
        formattedData.append("\n출처: KOTRA 국가정보 (86개국 정보 제공)");
        
        return formattedData.toString();
    }
}
