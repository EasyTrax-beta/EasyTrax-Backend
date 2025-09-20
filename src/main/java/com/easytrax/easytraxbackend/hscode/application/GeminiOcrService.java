package com.easytrax.easytraxbackend.hscode.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.config.GeminiConfig;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.hscode.api.dto.response.ProductOcrResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import java.util.Base64;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiOcrService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductOcrResult extractProductInfo(MultipartFile imageFile) {
        try {
            validateImageFile(imageFile);
            String base64Image = encodeImageToBase64(imageFile);
            String response = callGeminiApi(base64Image, imageFile.getContentType());
            return parseGeminiResponse(response);
        } catch (Exception e) {
            log.error("Gemini OCR 처리 중 오류 발생: {}", e.getMessage(), e);
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
        part1.put("text", createPrompt());

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

    private String createPrompt() {
        return """
                이미지에서 제품 정보를 추출하여 JSON 형태로 응답해주세요.
                
                다음 형식으로 응답해주세요:
                {
                  "productName": "제품명",
                  "purpose": "용도 (예: 식품, 화장품, 전자제품 등)",
                  "description": "제품에 대한 상세 설명",
                  "material": "재질 또는 원료 (예: 플라스틱, 금속, 면 등)",
                  "confidenceScore": 0.95
                }
                
                만약 이미지에서 제품 정보를 명확하게 식별할 수 없다면 confidenceScore를 낮춰주세요.
                텍스트가 없거나 제품이 명확하지 않은 경우에도 최대한 추정하여 응답해주세요.
                """;
    }

    private ProductOcrResult parseGeminiResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return parseProductInfoFromText(text);
                }
            }
            
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ProductOcrResult parseProductInfoFromText(String text) {
        try {
            String jsonText = extractJsonFromText(text);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            return ProductOcrResult.builder()
                    .productName(jsonNode.path("productName").asText("Unknown"))
                    .purpose(jsonNode.path("purpose").asText())
                    .description(jsonNode.path("description").asText())
                    .material(jsonNode.path("material").asText())
                    .confidenceScore(jsonNode.path("confidenceScore").asDouble(0.5))
                    .build();
        } catch (Exception e) {
            log.error("제품 정보 파싱 실패: {}", e.getMessage(), e);
            return ProductOcrResult.builder()
                    .productName("Unknown")
                    .purpose("")
                    .description("")
                    .material("")
                    .confidenceScore(0.1)
                    .build();
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
}