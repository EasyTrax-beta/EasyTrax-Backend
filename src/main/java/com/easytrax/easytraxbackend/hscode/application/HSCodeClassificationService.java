package com.easytrax.easytraxbackend.hscode.application;

import com.easytrax.easytraxbackend.global.config.GeminiConfig;
import com.easytrax.easytraxbackend.hscode.api.dto.response.HSCodeClassificationResult;
import com.easytrax.easytraxbackend.hscode.domain.FoodHSCode;
import com.easytrax.easytraxbackend.hscode.domain.HSCode;
import com.easytrax.easytraxbackend.hscode.domain.repository.HSCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HSCodeClassificationService {

    private final HSCodeRepository hsCodeRepository;
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HSCodeClassificationResult classifyHSCode(String productName, String purpose, 
                                                   String description, String material) {
        
        // Enum 기반 식품 HS 코드 매칭 시도
        List<FoodHSCode> foodMatches = findFoodHSCodeMatches(productName, purpose, description, material);
        
        if (!foodMatches.isEmpty()) {
            return createFoodHSCodeResult(foodMatches.get(0), productName, material);
        }
        
        // 기존 DB 기반 매칭 (fallback)
        List<HSCode> keywordMatchedCodes = findByKeywordMatching(productName, purpose, description, material);
        
        if (keywordMatchedCodes.isEmpty()) {
            return createLowConfidenceResult();
        }
        
        HSCodeClassificationResult aiResult = classifyWithAI(productName, purpose, description, material, keywordMatchedCodes);
        
        return enhanceWithKeywordScore(aiResult, keywordMatchedCodes, productName, material);
    }

    private List<HSCode> findByKeywordMatching(String productName, String purpose, String description, String material) {
        Set<HSCode> matchedCodes = new HashSet<>();
        
        if (productName != null && !productName.trim().isEmpty()) {
            matchedCodes.addAll(hsCodeRepository.findByProductNameContaining(productName.trim()));
        }
        
        if (material != null && !material.trim().isEmpty()) {
            matchedCodes.addAll(hsCodeRepository.findByMaterialContaining(material.trim()));
        }
        
        if (purpose != null && !purpose.trim().isEmpty()) {
            matchedCodes.addAll(hsCodeRepository.findByUsageContaining(purpose.trim()));
        }
        
        return new ArrayList<>(matchedCodes);
    }

    private HSCodeClassificationResult classifyWithAI(String productName, String purpose, 
                                                    String description, String material, 
                                                    List<HSCode> candidates) {
        try {
            String prompt = createClassificationPrompt(productName, purpose, description, material, candidates);
            String response = callGeminiForClassification(prompt);
            return parseClassificationResponse(response);
        } catch (Exception e) {
            log.error("AI 분류 중 오류 발생: {}", e.getMessage(), e);
            return createFallbackResult(candidates);
        }
    }

    private String createClassificationPrompt(String productName, String purpose, String description, 
                                            String material, List<HSCode> candidates) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 제품 정보를 분석하여 가장 적합한 HS 코드를 선택해주세요.\n\n");
        prompt.append("제품 정보:\n");
        prompt.append("- 제품명: ").append(productName).append("\n");
        prompt.append("- 용도: ").append(purpose != null ? purpose : "미정").append("\n");
        prompt.append("- 설명: ").append(description != null ? description : "미정").append("\n");
        prompt.append("- 재질: ").append(material != null ? material : "미정").append("\n\n");
        
        prompt.append("후보 HS 코드들:\n");
        for (int i = 0; i < Math.min(candidates.size(), 10); i++) {
            HSCode code = candidates.get(i);
            prompt.append(String.format("%d. %s - %s\n", i + 1, code.getHsCode(), code.getKoreanName()));
            if (code.getDescription() != null) {
                prompt.append("   설명: ").append(code.getDescription()).append("\n");
            }
        }
        
        prompt.append("\n다음 JSON 형식으로 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"selectedHsCode\": \"선택된 HS 코드\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"reason\": \"선택 이유\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    private String callGeminiForClassification(String prompt) {
        String url = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                geminiConfig.getBaseUrl(), geminiConfig.getModel(), geminiConfig.getKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));
        
        requestBody.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Gemini 분류 API 호출 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    private HSCodeClassificationResult parseClassificationResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return parseClassificationFromText(text);
                }
            }
            
            return createLowConfidenceResult();
        } catch (Exception e) {
            log.error("분류 응답 파싱 실패: {}", e.getMessage(), e);
            return createLowConfidenceResult();
        }
    }

    private HSCodeClassificationResult parseClassificationFromText(String text) {
        try {
            String jsonText = extractJsonFromText(text);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            String hsCode = jsonNode.path("selectedHsCode").asText();
            double confidence = jsonNode.path("confidence").asDouble(0.5);
            String reason = jsonNode.path("reason").asText("AI 분류 결과");
            
            return HSCodeClassificationResult.builder()
                    .hsCode(hsCode)
                    .confidence(confidence)
                    .reason(reason)
                    .build();
        } catch (Exception e) {
            log.error("분류 텍스트 파싱 실패: {}", e.getMessage(), e);
            return createLowConfidenceResult();
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

    private HSCodeClassificationResult enhanceWithKeywordScore(HSCodeClassificationResult aiResult, 
                                                             List<HSCode> keywordMatches, 
                                                             String productName, String material) {
        
        double keywordScore = calculateKeywordMatchScore(keywordMatches, productName, material);
        double enhancedConfidence = (aiResult.confidence() * 0.7) + (keywordScore * 0.3);
        
        return HSCodeClassificationResult.builder()
                .hsCode(aiResult.hsCode())
                .confidence(Math.min(enhancedConfidence, 1.0))
                .reason(aiResult.reason() + " (키워드 매칭 보정)")
                .build();
    }

    private double calculateKeywordMatchScore(List<HSCode> matches, String productName, String material) {
        if (matches.isEmpty()) return 0.0;
        
        double maxScore = 0.0;
        for (HSCode code : matches) {
            double score = 0.0;
            
            if (productName != null && code.getKoreanName().toLowerCase().contains(productName.toLowerCase())) {
                score += 0.4;
            }
            
            if (material != null && code.getMaterialInfo() != null && 
                code.getMaterialInfo().toLowerCase().contains(material.toLowerCase())) {
                score += 0.3;
            }
            
            if (code.getKeywords() != null) {
                for (String keyword : code.getKeywords()) {
                    if (productName != null && productName.toLowerCase().contains(keyword.toLowerCase())) {
                        score += 0.2;
                    }
                }
            }
            
            maxScore = Math.max(maxScore, score);
        }
        
        return Math.min(maxScore, 1.0);
    }

    private HSCodeClassificationResult createFallbackResult(List<HSCode> candidates) {
        if (!candidates.isEmpty()) {
            HSCode firstCandidate = candidates.get(0);
            return HSCodeClassificationResult.builder()
                    .hsCode(firstCandidate.getHsCode())
                    .confidence(0.6)
                    .reason("키워드 매칭 기반 추정")
                    .build();
        }
        
        return createLowConfidenceResult();
    }

    private List<FoodHSCode> findFoodHSCodeMatches(String productName, String purpose, String description, String material) {
        Map<FoodHSCode, Integer> scoreMap = new HashMap<>();
        
        // 제품명이 가장 높은 우선순위 (점수 4)
        if (productName != null && !productName.trim().isEmpty()) {
            List<FoodHSCode> productMatches = FoodHSCode.findByKeyword(productName);
            for (FoodHSCode food : productMatches) {
                scoreMap.put(food, scoreMap.getOrDefault(food, 0) + 4);
            }
        }
        
        // 재질 정보 (점수 3)
        if (material != null && !material.trim().isEmpty()) {
            List<FoodHSCode> materialMatches = FoodHSCode.findByMaterial(material);
            for (FoodHSCode food : materialMatches) {
                scoreMap.put(food, scoreMap.getOrDefault(food, 0) + 3);
            }
        }
        
        // 용도 (점수 2)
        if (purpose != null && !purpose.trim().isEmpty()) {
            List<FoodHSCode> purposeMatches = FoodHSCode.findByKeyword(purpose);
            for (FoodHSCode food : purposeMatches) {
                scoreMap.put(food, scoreMap.getOrDefault(food, 0) + 2);
            }
        }
        
        // 설명 (점수 1)
        if (description != null && !description.trim().isEmpty()) {
            List<FoodHSCode> descMatches = FoodHSCode.findByKeyword(description);
            for (FoodHSCode food : descMatches) {
                scoreMap.put(food, scoreMap.getOrDefault(food, 0) + 1);
            }
        }
        
        // 점수 순으로 정렬하여 반환
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<FoodHSCode, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private HSCodeClassificationResult createFoodHSCodeResult(FoodHSCode foodCode, String productName, String material) {
        double confidence = calculateFoodMatchConfidence(foodCode, productName, material);
        
        return HSCodeClassificationResult.builder()
                .hsCode(foodCode.getHsCode())
                .confidence(confidence)
                .reason(String.format("식품 카테고리 매칭: %s (%s)", foodCode.getKoreanName(), foodCode.getCategory()))
                .build();
    }

    private double calculateFoodMatchConfidence(FoodHSCode foodCode, String productName, String material) {
        double confidence = 0.6; // 기본 신뢰도
        
        // 제품명 정확 매칭
        if (productName != null) {
            for (String keyword : foodCode.getKeywords()) {
                if (productName.toLowerCase().contains(keyword.toLowerCase())) {
                    confidence += 0.2;
                    break;
                }
            }
        }
        
        // 재질 정보 매칭
        if (material != null && foodCode.getMaterialInfo().toLowerCase().contains(material.toLowerCase())) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 0.95); // 최대 95%
    }

    private HSCodeClassificationResult createLowConfidenceResult() {
        return HSCodeClassificationResult.builder()
                .hsCode("0000000000")
                .confidence(0.1)
                .reason("분류할 수 없음 - 수동 확인 필요")
                .build();
    }
}