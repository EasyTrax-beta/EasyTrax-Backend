package com.easytrax.easytraxbackend.nutritionlabel.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.config.GeminiConfig;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.request.NutritionLabelCreateRequest;
import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionLabelOcrService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NutritionLabelCreateRequest extractNutritionInfo(MultipartFile imageFile, Long projectId, LabelFormat labelFormat) {
        try {
            validateImageFile(imageFile);
            String base64Image = encodeImageToBase64(imageFile);
            String response = callGeminiApi(base64Image, imageFile.getContentType());
            return parseNutritionResponse(response, projectId, labelFormat);
        } catch (Exception e) {
            log.error("영양성분표 OCR 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(ErrorStatus.FILE_IS_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new GeneralException(ErrorStatus.INVALID_FILE_TYPE);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new GeneralException(ErrorStatus.FILE_SIZE_EXCEEDED);
        }
    }

    private String encodeImageToBase64(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String callGeminiApi(String base64Image, String mimeType) {
        String url = String.format("%s/v1beta/models/%s:generateContent",
                geminiConfig.getBaseUrl(), geminiConfig.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiConfig.getKey());

        Map<String, Object> requestBody = createGeminiRequestBody(base64Image, mimeType);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> createGeminiRequestBody(String base64Image, String mimeType) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part1 = new HashMap<>();
        part1.put("text", createNutritionPrompt());

        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);

        Map<String, Object> part2 = new HashMap<>();
        part2.put("inline_data", inlineData);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part1, part2));

        requestBody.put("contents", List.of(content));
        return requestBody;
    }

    private String createNutritionPrompt() {
        return """
                이미지에서 영양성분표(Nutrition Facts/营养成分表/栄養成分表) 정보를 추출하여 JSON 형태로 응답해주세요.
                
                다음 형식으로 응답해주세요:
                {
                  "productName": "제품명",
                  "servingSize": "1회 제공량 (예: 2/3 cup (55g))",
                  "servingsPerContainer": 8,
                  "calories": 230,
                  "caloriesFromFat": 72,
                  "totalFat": 8.0,
                  "saturatedFat": 1.0,
                  "transFat": 0.0,
                  "cholesterol": 0.0,
                  "sodium": 160.0,
                  "totalCarbohydrate": 37.0,
                  "dietaryFiber": 4.0,
                  "totalSugars": 12.0,
                  "addedSugars": 10.0,
                  "protein": 3.0,
                  "vitaminD": 2.0,
                  "calcium": 260.0,
                  "iron": 8.0,
                  "potassium": 235.0,
                  "vitaminA": 0.0,
                  "vitaminC": 0.0,
                  "confidenceScore": 0.95
                }
                
                주의사항:
                - 숫자 값은 정확히 추출하되, 없는 값은 0.0으로 설정
                - 단위는 제거하고 숫자만 추출 (mg, g, mcg 등 제거)
                - 제품명이 없으면 "Unknown Product"로 설정
                - 1회 제공량이 없으면 "1 serving"으로 설정
                - confidenceScore는 추출 정확도를 0-1 사이로 표시
                """;
    }

    private NutritionLabelCreateRequest parseNutritionResponse(String response, Long projectId, LabelFormat labelFormat) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return parseNutritionFromText(text, projectId, labelFormat);
                }
            }
            
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private NutritionLabelCreateRequest parseNutritionFromText(String text, Long projectId, LabelFormat labelFormat) {
        try {
            String jsonText = extractJsonFromText(text);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            return new NutritionLabelCreateRequest(
                    projectId,
                    jsonNode.path("productName").asText("Unknown Product"),
                    jsonNode.path("servingSize").asText("1 serving"),
                    jsonNode.path("servingsPerContainer").asInt(1),
                    jsonNode.path("calories").asInt(0),
                    jsonNode.path("caloriesFromFat").asInt(0),
                    getBigDecimalValue(jsonNode, "totalFat"),
                    getBigDecimalValue(jsonNode, "saturatedFat"),
                    getBigDecimalValue(jsonNode, "transFat"),
                    getBigDecimalValue(jsonNode, "cholesterol"),
                    getBigDecimalValue(jsonNode, "sodium"),
                    getBigDecimalValue(jsonNode, "totalCarbohydrate"),
                    getBigDecimalValue(jsonNode, "dietaryFiber"),
                    getBigDecimalValue(jsonNode, "totalSugars"),
                    getBigDecimalValue(jsonNode, "addedSugars"),
                    getBigDecimalValue(jsonNode, "protein"),
                    getBigDecimalValue(jsonNode, "vitaminD"),
                    getBigDecimalValue(jsonNode, "calcium"),
                    getBigDecimalValue(jsonNode, "iron"),
                    getBigDecimalValue(jsonNode, "potassium"),
                    getBigDecimalValue(jsonNode, "vitaminA"),
                    getBigDecimalValue(jsonNode, "vitaminC"),
                    labelFormat
            );
        } catch (Exception e) {
            log.error("영양성분 정보 파싱 실패: {}", e.getMessage(), e);
            return createDefaultNutritionRequest(projectId, labelFormat);
        }
    }

    private BigDecimal getBigDecimalValue(JsonNode jsonNode, String fieldName) {
        double value = jsonNode.path(fieldName).asDouble(0.0);
        return BigDecimal.valueOf(value);
    }

    private String extractJsonFromText(String text) {
        int jsonStart = text.indexOf("{");
        int jsonEnd = text.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }
        
        return text;
    }

    private NutritionLabelCreateRequest createDefaultNutritionRequest(Long projectId, LabelFormat labelFormat) {
        return new NutritionLabelCreateRequest(
                projectId, "Unknown Product", "1 serving", 1, 0, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                labelFormat
        );
    }
}