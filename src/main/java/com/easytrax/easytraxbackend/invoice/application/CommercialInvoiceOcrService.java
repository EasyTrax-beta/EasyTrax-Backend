package com.easytrax.easytraxbackend.invoice.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.config.GeminiConfig;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.invoice.api.dto.request.CommercialInvoiceCreateRequest;
import com.easytrax.easytraxbackend.invoice.api.dto.request.CommercialInvoiceItemRequest;
import com.easytrax.easytraxbackend.invoice.domain.InvoiceFormat;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommercialInvoiceOcrService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommercialInvoiceCreateRequest extractInvoiceInfo(MultipartFile imageFile, Long projectId, InvoiceFormat invoiceFormat) {
        try {
            validateImageFile(imageFile);
            String base64Image = encodeImageToBase64(imageFile);
            String response = callGeminiApi(base64Image, imageFile.getContentType());
            return parseInvoiceResponse(response, projectId, invoiceFormat);
        } catch (Exception e) {
            log.error("상업송장 OCR 처리 중 오류 발생: {}", e.getMessage(), e);
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
        part1.put("text", createInvoicePrompt());

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

    private String createInvoicePrompt() {
        return """
                이미지에서 상업송장(Commercial Invoice/商业发票/商業インボイス) 정보를 추출하여 JSON 형태로 응답해주세요.
                
                다음 형식으로 응답해주세요:
                {
                  "invoiceNumber": "송장 번호",
                  "invoiceDate": "2025-09-13",
                  "shipperSellerName": "발송인/판매자 회사명",
                  "shipperSellerAddress": "발송인/판매자 주소",
                  "shipperSellerPhone": "발송인/판매자 전화번호",
                  "consigneeName": "수취인 이름",
                  "consigneeAddress": "수취인 주소",
                  "buyerName": "구매자 회사명",
                  "buyerAddress": "구매자 주소",
                  "buyerPhone": "구매자 전화번호",
                  "lcNumber": "L/C 번호",
                  "lcDate": "2025-09-13",
                  "departureDate": "2025-09-15",
                  "vesselFlight": "선박/항공편",
                  "fromCountry": "출발 국가",
                  "toDestination": "목적지",
                  "shippingMarks": "선적 마크",
                  "termsOfDelivery": "인도 조건 (FOB, CIF 등)",
                  "paymentTerms": "결제 조건 (T/T, L/C 등)",
                  "otherReferences": "기타 참조사항",
                  "items": [
                    {
                      "packageCount": 10,
                      "packageType": "Boxes",
                      "goodsDescription": "상품 설명",
                      "quantity": 240,
                      "unitPrice": 2.50
                    }
                  ],
                  "confidenceScore": 0.95
                }
                
                주의사항:
                - 날짜는 YYYY-MM-DD 형식으로 변환
                - 금액은 숫자만 추출 (통화기호 제거)
                - 없는 정보는 빈 문자열 또는 "N/A"로 설정
                - items 배열에는 최소 1개 항목 포함
                - confidenceScore는 추출 정확도를 0-1 사이로 표시
                """;
    }

    private CommercialInvoiceCreateRequest parseInvoiceResponse(String response, Long projectId, InvoiceFormat invoiceFormat) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return parseInvoiceFromText(text, projectId, invoiceFormat);
                }
            }
            
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CommercialInvoiceCreateRequest parseInvoiceFromText(String text, Long projectId, InvoiceFormat invoiceFormat) {
        try {
            String jsonText = extractJsonFromText(text);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            List<CommercialInvoiceItemRequest> items = parseItems(jsonNode.path("items"));
            
            return new CommercialInvoiceCreateRequest(
                    projectId,
                    jsonNode.path("invoiceNumber").asText("INV-" + System.currentTimeMillis()),
                    parseDate(jsonNode.path("invoiceDate").asText()),
                    jsonNode.path("shipperSellerName").asText("Unknown Shipper"),
                    jsonNode.path("shipperSellerAddress").asText("Unknown Address"),
                    getStringOrNull(jsonNode, "shipperSellerPhone"),
                    getStringOrNull(jsonNode, "consigneeName"),
                    getStringOrNull(jsonNode, "consigneeAddress"),
                    jsonNode.path("buyerName").asText("Unknown Buyer"),
                    jsonNode.path("buyerAddress").asText("Unknown Address"),
                    getStringOrNull(jsonNode, "buyerPhone"),
                    getStringOrNull(jsonNode, "lcNumber"),
                    parseDate(jsonNode.path("lcDate").asText()),
                    parseDate(jsonNode.path("departureDate").asText()),
                    getStringOrNull(jsonNode, "vesselFlight"),
                    jsonNode.path("fromCountry").asText("Korea"),
                    getStringOrNull(jsonNode, "toDestination"),
                    getStringOrNull(jsonNode, "shippingMarks"),
                    getStringOrNull(jsonNode, "termsOfDelivery"),
                    getStringOrNull(jsonNode, "paymentTerms"),
                    getStringOrNull(jsonNode, "otherReferences"),
                    invoiceFormat,
                    items
            );
        } catch (Exception e) {
            log.error("상업송장 정보 파싱 실패: {}", e.getMessage(), e);
            return createDefaultInvoiceRequest(projectId, invoiceFormat);
        }
    }

    private List<CommercialInvoiceItemRequest> parseItems(JsonNode itemsNode) {
        List<CommercialInvoiceItemRequest> items = new ArrayList<>();
        
        if (itemsNode.isArray() && itemsNode.size() > 0) {
            for (JsonNode itemNode : itemsNode) {
                CommercialInvoiceItemRequest item = new CommercialInvoiceItemRequest(
                        itemNode.path("packageCount").asInt(1),
                        itemNode.path("packageType").asText("Package"),
                        itemNode.path("goodsDescription").asText("Unknown Product"),
                        itemNode.path("quantity").asInt(1),
                        BigDecimal.valueOf(itemNode.path("unitPrice").asDouble(0.0))
                );
                items.add(item);
            }
        } else {
            items.add(new CommercialInvoiceItemRequest(
                    1, "Package", "Unknown Product", 1, BigDecimal.ZERO));
        }
        
        return items;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "N/A".equals(dateStr)) {
            return LocalDate.now();
        }
        
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String getStringOrNull(JsonNode jsonNode, String fieldName) {
        String value = jsonNode.path(fieldName).asText();
        return (value == null || value.trim().isEmpty() || "N/A".equals(value)) ? null : value;
    }

    private String extractJsonFromText(String text) {
        int jsonStart = text.indexOf("{");
        int jsonEnd = text.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }
        
        return text;
    }

    private CommercialInvoiceCreateRequest createDefaultInvoiceRequest(Long projectId, InvoiceFormat invoiceFormat) {
        List<CommercialInvoiceItemRequest> defaultItems = List.of(
                new CommercialInvoiceItemRequest(1, "Package", "Unknown Product", 1, BigDecimal.ZERO)
        );
        
        return new CommercialInvoiceCreateRequest(
                projectId, "INV-" + System.currentTimeMillis(), LocalDate.now(),
                "Unknown Shipper", "Unknown Address", null, null, null,
                "Unknown Buyer", "Unknown Address", null, null, null,
                null, null, "Korea", null, null, null, null, null,
                invoiceFormat, defaultItems
        );
    }
}