package com.easytrax.easytraxbackend.compliance.application;

import com.easytrax.easytraxbackend.compliance.api.dto.request.ComplianceAnalysisRequest;
import com.easytrax.easytraxbackend.compliance.api.dto.request.InvoiceComplianceAnalysisRequest;
import com.easytrax.easytraxbackend.compliance.api.dto.response.ComplianceAnalysisResponse;
import com.easytrax.easytraxbackend.compliance.domain.TradeType;
import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.config.GeminiConfig;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.nutritionlabel.application.NutritionLabelPdfService;
import com.easytrax.easytraxbackend.nutritionlabel.application.NutritionLabelService;
import com.easytrax.easytraxbackend.nutritionlabel.domain.NutritionLabel;
import com.easytrax.easytraxbackend.invoice.application.CommercialInvoiceService;
import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceAnalysisService {

    private final NutritionLabelService nutritionLabelService;
    private final CommercialInvoiceService commercialInvoiceService;
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ComplianceAnalysisResponse analyzeCompliance(ComplianceAnalysisRequest request, Long userId) {
        try {
            log.info("준수성 분석 시작 - 영양성분표 ID: {}", 
                    request.nutritionLabelId());
            
            NutritionLabel nutritionLabel = nutritionLabelService.findNutritionLabelByIdAndUserId(
                    request.nutritionLabelId(), userId);
            log.info("영양성분표 조회 완료 - 제품명: {}", nutritionLabel.getProductName());
            
            // 프로젝트에서 목적지 국가 자동 추출
            log.info("영양성분표 ID: {}, 연결된 프로젝트: {}", request.nutritionLabelId(), 
                    nutritionLabel.getProject() != null ? nutritionLabel.getProject().getId() : "null");
            String destinationCountry = extractDestinationCountryFromProject(nutritionLabel.getProject());
            log.info("목적지 국가 자동 추출: {}", destinationCountry);
            
            // 자동 추출된 국가 정보로 새로운 request 생성
            ComplianceAnalysisRequest updatedRequest = new ComplianceAnalysisRequest(
                    request.nutritionLabelId(), request.productCategory());
            
            // PDF 대신 텍스트 기반으로 분석 (Gemini API의 PDF 지원 제한으로 인해)
            String nutritionText = createNutritionText(nutritionLabel);
            log.info("영양성분표 텍스트 생성 완료 - 길이: {}", nutritionText.length());
            
            String response = callGeminiApiForComplianceTextWithCountry(nutritionText, destinationCountry, request.productCategory());
            log.info("Gemini API 호출 완료");
            
            return parseComplianceResponseWithCountry(response, destinationCountry, request.productCategory(), nutritionLabel);
            
        } catch (Exception e) {
            log.error("준수성 분석 중 오류 발생: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ComplianceAnalysisResponse analyzeInvoiceCompliance(InvoiceComplianceAnalysisRequest request, Long userId) {
        try {
            log.info("상업송장 준수성 분석 시작 - 송장 ID: {}", 
                    request.commercialInvoiceId());
            
            CommercialInvoice commercialInvoice = commercialInvoiceService.findCommercialInvoiceByIdAndUserId(
                    request.commercialInvoiceId(), userId);
            log.info("상업송장 조회 완료 - 송장번호: {}", commercialInvoice.getInvoiceNumber());
            
            // 프로젝트에서 목적지 국가 자동 추출
            log.info("상업송장 ID: {}, 연결된 프로젝트: {}", request.commercialInvoiceId(), 
                    commercialInvoice.getProject() != null ? commercialInvoice.getProject().getId() : "null");
            String destinationCountry = extractDestinationCountryFromProject(commercialInvoice.getProject());
            log.info("목적지 국가 자동 추출: {}", destinationCountry);
            
            // 자동 추출된 국가 정보로 새로운 request 생성
            InvoiceComplianceAnalysisRequest updatedRequest = new InvoiceComplianceAnalysisRequest(
                    request.commercialInvoiceId(), request.tradeType());
            
            String invoiceText = createInvoiceText(commercialInvoice);
            log.info("상업송장 텍스트 생성 완료 - 길이: {}", invoiceText.length());
            
            String response = callGeminiApiForInvoiceComplianceWithCountry(invoiceText, destinationCountry, request.tradeType());
            log.info("Gemini API 호출 완료");
            
            return parseInvoiceComplianceResponseWithCountry(response, destinationCountry, request.tradeType(), commercialInvoice);
            
        } catch (Exception e) {
            log.error("상업송장 준수성 분석 중 오류 발생: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createNutritionText(NutritionLabel nutritionLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 영양성분표 정보 ===\n");
        sb.append("제품명: ").append(nutritionLabel.getProductName()).append("\n");
        sb.append("1회 제공량: ").append(nutritionLabel.getServingSize()).append("\n");
        sb.append("포장당 제공량: ").append(nutritionLabel.getServingsPerContainer()).append("\n");
        sb.append("라벨 형식: ").append(nutritionLabel.getLabelFormat()).append("\n\n");
        
        sb.append("=== 영양성분 ===\n");
        sb.append("칼로리: ").append(nutritionLabel.getCalories()).append(" kcal\n");
        if (nutritionLabel.getTotalFat() != null) {
            sb.append("총 지방: ").append(nutritionLabel.getTotalFat()).append("g\n");
        }
        if (nutritionLabel.getSaturatedFat() != null) {
            sb.append("포화지방: ").append(nutritionLabel.getSaturatedFat()).append("g\n");
        }
        if (nutritionLabel.getTransFat() != null) {
            sb.append("트랜스지방: ").append(nutritionLabel.getTransFat()).append("g\n");
        }
        if (nutritionLabel.getCholesterol() != null) {
            sb.append("콜레스테롤: ").append(nutritionLabel.getCholesterol()).append("mg\n");
        }
        if (nutritionLabel.getSodium() != null) {
            sb.append("나트륨: ").append(nutritionLabel.getSodium()).append("mg\n");
        }
        if (nutritionLabel.getTotalCarbohydrate() != null) {
            sb.append("총 탄수화물: ").append(nutritionLabel.getTotalCarbohydrate()).append("g\n");
        }
        if (nutritionLabel.getDietaryFiber() != null) {
            sb.append("식이섬유: ").append(nutritionLabel.getDietaryFiber()).append("g\n");
        }
        if (nutritionLabel.getTotalSugars() != null) {
            sb.append("총 당류: ").append(nutritionLabel.getTotalSugars()).append("g\n");
        }
        if (nutritionLabel.getAddedSugars() != null) {
            sb.append("첨가당: ").append(nutritionLabel.getAddedSugars()).append("g\n");
        }
        if (nutritionLabel.getProtein() != null) {
            sb.append("단백질: ").append(nutritionLabel.getProtein()).append("g\n");
        }
        if (nutritionLabel.getVitaminA() != null) {
            sb.append("비타민 A: ").append(nutritionLabel.getVitaminA()).append("mcg\n");
        }
        if (nutritionLabel.getVitaminC() != null) {
            sb.append("비타민 C: ").append(nutritionLabel.getVitaminC()).append("mg\n");
        }
        if (nutritionLabel.getCalcium() != null) {
            sb.append("칼슘: ").append(nutritionLabel.getCalcium()).append("mg\n");
        }
        if (nutritionLabel.getIron() != null) {
            sb.append("철분: ").append(nutritionLabel.getIron()).append("mg\n");
        }
        
        return sb.toString();
    }

    private List<ComplianceAnalysisResponse.CategoryComplianceScore> parseCategoryScores(JsonNode scoresNode) {
        List<ComplianceAnalysisResponse.CategoryComplianceScore> scores = new ArrayList<>();
        
        if (scoresNode.isArray()) {
            for (JsonNode scoreNode : scoresNode) {
                scores.add(new ComplianceAnalysisResponse.CategoryComplianceScore(
                        scoreNode.path("categoryName").asText(),
                        scoreNode.path("score").asInt(50),
                        scoreNode.path("status").asText("경고")
                ));
            }
        }
        
        return scores;
    }

    private List<ComplianceAnalysisResponse.ComplianceIssue> parseIssues(JsonNode issuesNode) {
        List<ComplianceAnalysisResponse.ComplianceIssue> issues = new ArrayList<>();
        
        if (issuesNode.isArray()) {
            for (JsonNode issueNode : issuesNode) {
                issues.add(new ComplianceAnalysisResponse.ComplianceIssue(
                        issueNode.path("issueType").asText(),
                        issueNode.path("severity").asText("MEDIUM"),
                        issueNode.path("description").asText(),
                        issueNode.path("regulation").asText()
                ));
            }
        }
        
        return issues;
    }

    private List<ComplianceAnalysisResponse.ImprovementSuggestion> parseImprovements(JsonNode improvementsNode) {
        List<ComplianceAnalysisResponse.ImprovementSuggestion> improvements = new ArrayList<>();
        
        if (improvementsNode.isArray()) {
            for (JsonNode improvementNode : improvementsNode) {
                improvements.add(new ComplianceAnalysisResponse.ImprovementSuggestion(
                        improvementNode.path("category").asText(),
                        improvementNode.path("suggestion").asText(),
                        improvementNode.path("priority").asText("MEDIUM")
                ));
            }
        }
        
        return improvements;
    }

    private ComplianceAnalysisResponse.ComplianceStatus parseComplianceStatus(String status) {
        try {
            return ComplianceAnalysisResponse.ComplianceStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return ComplianceAnalysisResponse.ComplianceStatus.WARNING;
        }
    }


    private String extractJsonFromText(String text) {
        int jsonStart = text.indexOf("{");
        int jsonEnd = text.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }
        
        return text;
    }

    private String getCountryName(String countryCode) {
        Map<String, String> countryNames = Map.of(
                "CN", "중국",
                "US", "미국",
                "JP", "일본",
                "EU", "유럽연합"
        );
        return countryNames.getOrDefault(countryCode, countryCode);
    }

    private String createInvoiceText(CommercialInvoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 상업송장 정보 ===\n");
        sb.append("송장번호: ").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("송장일자: ").append(invoice.getInvoiceDate()).append("\n");
        sb.append("송장 포맷: ").append(invoice.getInvoiceFormat()).append("\n\n");
        
        sb.append("=== 발송인/판매자 ===\n");
        sb.append("이름: ").append(invoice.getShipperSellerName()).append("\n");
        sb.append("주소: ").append(invoice.getShipperSellerAddress()).append("\n");
        if (invoice.getShipperSellerPhone() != null) {
            sb.append("연락처: ").append(invoice.getShipperSellerPhone()).append("\n");
        }
        
        sb.append("\n=== 수취인 ===\n");
        if (invoice.getConsigneeName() != null) {
            sb.append("이름: ").append(invoice.getConsigneeName()).append("\n");
        }
        if (invoice.getConsigneeAddress() != null) {
            sb.append("주소: ").append(invoice.getConsigneeAddress()).append("\n");
        }
        
        sb.append("\n=== 구매자 ===\n");
        sb.append("이름: ").append(invoice.getBuyerName()).append("\n");
        sb.append("주소: ").append(invoice.getBuyerAddress()).append("\n");
        if (invoice.getBuyerPhone() != null) {
            sb.append("연락처: ").append(invoice.getBuyerPhone()).append("\n");
        }
        
        sb.append("\n=== 무역 조건 ===\n");
        if (invoice.getLcNumber() != null) {
            sb.append("L/C 번호: ").append(invoice.getLcNumber()).append("\n");
        }
        if (invoice.getLcDate() != null) {
            sb.append("L/C 일자: ").append(invoice.getLcDate()).append("\n");
        }
        if (invoice.getDepartureDate() != null) {
            sb.append("출발일: ").append(invoice.getDepartureDate()).append("\n");
        }
        if (invoice.getVesselFlight() != null) {
            sb.append("선박/항공편: ").append(invoice.getVesselFlight()).append("\n");
        }
        sb.append("출발국가: ").append(invoice.getFromCountry()).append("\n");
        if (invoice.getToDestination() != null) {
            sb.append("목적지: ").append(invoice.getToDestination()).append("\n");
        }
        if (invoice.getTermsOfDelivery() != null) {
            sb.append("인도조건: ").append(invoice.getTermsOfDelivery()).append("\n");
        }
        if (invoice.getPaymentTerms() != null) {
            sb.append("결제조건: ").append(invoice.getPaymentTerms()).append("\n");
        }
        
        sb.append("\n=== 상품 정보 ===\n");
        sb.append("총 금액: $").append(invoice.getTotalAmount()).append("\n");
        sb.append("상품 목록:\n");
        
        invoice.getItems().forEach(item -> {
            sb.append("- ").append(item.getGoodsDescription()).append("\n");
            sb.append("  포장: ").append(item.getPackageCount()).append(" ").append(item.getPackageType()).append("\n");
            sb.append("  수량: ").append(item.getQuantity()).append("\n");
            sb.append("  단가: $").append(item.getUnitPrice()).append("\n");
            sb.append("  금액: $").append(item.getAmount()).append("\n\n");
        });
        
        return sb.toString();
    }

    private String extractDestinationCountryFromProject(com.easytrax.easytraxbackend.project.domain.Project project) {
        if (project == null || project.getTargetCountry() == null) {
            throw new GeneralException(ErrorStatus.PROJECT_TARGET_COUNTRY_NOT_FOUND);
        }
        
        String countryCode = project.getTargetCountry().getCountryCode();
        log.info("프로젝트 ID: {}, 목적지 국가: {}, 국가 코드: {}", 
                project.getId(), project.getTargetCountry(), countryCode);
        
        return countryCode;
    }

    private String callGeminiApiForComplianceTextWithCountry(String nutritionText, String destinationCountry, String productCategory) {
        String url = String.format("%s/v1beta/models/%s:generateContent",
                geminiConfig.getBaseUrl(), geminiConfig.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiConfig.getKey());

        Map<String, Object> requestBody = createComplianceTextRequestBodyWithCountry(nutritionText, destinationCountry, productCategory);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> createComplianceTextRequestBodyWithCountry(String nutritionText, String destinationCountry, String productCategory) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part1 = new HashMap<>();
        part1.put("text", createComplianceTextPromptWithCountry(nutritionText, destinationCountry));

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part1));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private String createComplianceTextPromptWithCountry(String nutritionText, String destinationCountry) {
        String countryName = getCountryName(destinationCountry);
        
        log.info("AI 분석 요청 - 목적지 국가: {} ({})", countryName, destinationCountry);
        
        return String.format("""
                다음 영양성분표 정보를 바탕으로 %s(%s) 수출을 위한 라벨링 규정 준수 여부를 분석해주세요.
                
                %s
                
                분석 항목:
                1. 라벨링 규정 (필수 표시 항목, 글자 크기, 언어 요구사항)
                2. 영양성분표 (표시 형식, 단위, Daily Value 기준)
                3. 원산지 표시 (Country of Origin 표기)
                4. 인증서 요구사항 (필요한 인증 마크나 승인번호)
                
                다음 JSON 형식으로 응답해주세요:
                {
                  "overallCompliancePercentage": 85,
                  "complianceStatus": "WARNING",
                  "estimatedCompletionMinutes": 30,
                  "categoryScores": [
                    {
                      "categoryName": "라벨링 규정",
                      "score": 70,
                      "status": "경고"
                    },
                    {
                      "categoryName": "영양성분표",
                      "score": 90,
                      "status": "양호"
                    },
                    {
                      "categoryName": "원산지 표시",
                      "score": 60,
                      "status": "위험"
                    },
                    {
                      "categoryName": "인증서 요구사항",
                      "score": 80,
                      "status": "경고"
                    }
                  ],
                  "issues": [
                    {
                      "issueType": "제품명이 누락되었습니다",
                      "severity": "HIGH",
                      "description": "제품명을 명확히 표기해주세요",
                      "regulation": "GB 28050-2011"
                    }
                  ],
                  "improvements": [
                    {
                      "category": "발견된 문제점을 수정하세요",
                      "suggestion": "모든 필수 정보를 정확히 표기해주세요",
                      "priority": "HIGH"
                    }
                  ]
                }
                
                complianceStatus는 다음 중 하나를 사용하세요:
                - GOOD: 90%% 이상
                - WARNING: 70-89%%
                - HIGH_RISK: 70%% 미만
                """, getCountryName(destinationCountry), destinationCountry, nutritionText);
    }

    private ComplianceAnalysisResponse parseComplianceResponseWithCountry(String response, String destinationCountry, String productCategory, NutritionLabel nutritionLabel) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return parseComplianceFromTextWithCountry(text, destinationCountry, productCategory, nutritionLabel);
                }
            }
            
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ComplianceAnalysisResponse parseComplianceFromTextWithCountry(String text, String destinationCountry, String productCategory, NutritionLabel nutritionLabel) {
        try {
            String jsonText = extractJsonFromText(text);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            List<ComplianceAnalysisResponse.CategoryComplianceScore> categoryScores = 
                    parseCategoryScores(jsonNode.path("categoryScores"));
            
            List<ComplianceAnalysisResponse.ComplianceIssue> issues = 
                    parseIssues(jsonNode.path("issues"));
            
            List<ComplianceAnalysisResponse.ImprovementSuggestion> improvements = 
                    parseImprovements(jsonNode.path("improvements"));
            
            return new ComplianceAnalysisResponse(
                    null,
                    nutritionLabel.getId(),
                    destinationCountry,
                    productCategory,
                    jsonNode.path("overallCompliancePercentage").asInt(50),
                    parseComplianceStatus(jsonNode.path("complianceStatus").asText("WARNING")),
                    jsonNode.path("estimatedCompletionMinutes").asInt(60),
                    LocalDateTime.now(),
                    categoryScores,
                    issues,
                    improvements
            );
        } catch (Exception e) {
            log.error("준수성 정보 파싱 실패: {}", e.getMessage(), e);
            return createDefaultComplianceResponseWithCountry(destinationCountry, productCategory, nutritionLabel);
        }
    }

    private ComplianceAnalysisResponse createDefaultComplianceResponseWithCountry(String destinationCountry, String productCategory, NutritionLabel nutritionLabel) {
        return new ComplianceAnalysisResponse(
                null,
                nutritionLabel.getId(),
                destinationCountry,
                productCategory,
                50,
                ComplianceAnalysisResponse.ComplianceStatus.WARNING,
                60,
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private String callGeminiApiForInvoiceComplianceWithCountry(String invoiceText, String destinationCountry, TradeType tradeType) {
        String url = String.format("%s/v1beta/models/%s:generateContent",
                geminiConfig.getBaseUrl(), geminiConfig.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiConfig.getKey());

        Map<String, Object> requestBody = createInvoiceComplianceRequestBodyWithCountry(invoiceText, destinationCountry, tradeType);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> createInvoiceComplianceRequestBodyWithCountry(String invoiceText, String destinationCountry, TradeType tradeType) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part1 = new HashMap<>();
        part1.put("text", createInvoiceCompliancePromptWithCountry(invoiceText, destinationCountry));

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part1));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private String createInvoiceCompliancePromptWithCountry(String invoiceText, String destinationCountry) {
        String countryName = getCountryName(destinationCountry);
        
        log.info("상업송장 AI 분석 요청 - 목적지 국가: {} ({})", countryName, destinationCountry);
        
        return String.format("""
                다음 상업송장 정보를 바탕으로 %s(%s) 수출을 위한 무역 문서 규정 준수 여부를 분석해주세요.
                
                %s
                
                분석 항목:
                1. 무역 문서 규정 (필수 기재 항목, 양식 요구사항)
                2. 관세 및 세관 규정 (HS 코드, 원산지 증명)
                3. 송금 및 외환 규정 (결제 조건, 금액 표기)
                4. 상품 정보 정확성 (품목 설명, 수량, 가격)
                
                다음 JSON 형식으로 응답해주세요:
                {
                  "overallCompliancePercentage": 80,
                  "complianceStatus": "WARNING",
                  "estimatedCompletionMinutes": 45,
                  "categoryScores": [
                    {
                      "categoryName": "무역 문서 규정",
                      "score": 75,
                      "status": "경고"
                    },
                    {
                      "categoryName": "관세 및 세관 규정",
                      "score": 70,
                      "status": "경고"
                    },
                    {
                      "categoryName": "송금 및 외환 규정",
                      "score": 90,
                      "status": "양호"
                    },
                    {
                      "categoryName": "상품 정보 정확성",
                      "score": 85,
                      "status": "양호"
                    }
                  ],
                  "issues": [
                    {
                      "issueType": "HS 코드 누락",
                      "severity": "HIGH",
                      "description": "각 상품에 대한 HS 코드가 누락되었습니다",
                      "regulation": "관세법"
                    }
                  ],
                  "improvements": [
                    {
                      "category": "관세 정보 보완",
                      "suggestion": "각 상품에 정확한 HS 코드를 추가하세요",
                      "priority": "HIGH"
                    }
                  ]
                }
                
                complianceStatus는 다음 중 하나를 사용하세요:
                - GOOD: 90%% 이상
                - WARNING: 70-89%%
                - HIGH_RISK: 70%% 미만
                """, getCountryName(destinationCountry), destinationCountry, invoiceText);
    }

    private ComplianceAnalysisResponse parseInvoiceComplianceResponseWithCountry(String response, String destinationCountry, TradeType tradeType, CommercialInvoice invoice) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return parseInvoiceComplianceFromTextWithCountry(text, destinationCountry, tradeType, invoice);
                }
            }
            
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ComplianceAnalysisResponse parseInvoiceComplianceFromTextWithCountry(String text, String destinationCountry, TradeType tradeType, CommercialInvoice invoice) {
        try {
            String jsonText = extractJsonFromText(text);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            List<ComplianceAnalysisResponse.CategoryComplianceScore> categoryScores = 
                    parseCategoryScores(jsonNode.path("categoryScores"));
            
            List<ComplianceAnalysisResponse.ComplianceIssue> issues = 
                    parseIssues(jsonNode.path("issues"));
            
            List<ComplianceAnalysisResponse.ImprovementSuggestion> improvements = 
                    parseImprovements(jsonNode.path("improvements"));
            
            return new ComplianceAnalysisResponse(
                    null,
                    null, // 상업송장의 경우 nutritionLabelId는 null
                    destinationCountry,
                    tradeType.name(),
                    jsonNode.path("overallCompliancePercentage").asInt(50),
                    parseComplianceStatus(jsonNode.path("complianceStatus").asText("WARNING")),
                    jsonNode.path("estimatedCompletionMinutes").asInt(60),
                    LocalDateTime.now(),
                    categoryScores,
                    issues,
                    improvements
            );
        } catch (Exception e) {
            log.error("상업송장 준수성 정보 파싱 실패: {}", e.getMessage(), e);
            return createDefaultInvoiceComplianceResponseWithCountry(destinationCountry, tradeType, invoice);
        }
    }

    private ComplianceAnalysisResponse createDefaultInvoiceComplianceResponseWithCountry(String destinationCountry, TradeType tradeType, CommercialInvoice invoice) {
        return new ComplianceAnalysisResponse(
                null,
                null,
                destinationCountry,
                tradeType.name(),
                50,
                ComplianceAnalysisResponse.ComplianceStatus.WARNING,
                60,
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
