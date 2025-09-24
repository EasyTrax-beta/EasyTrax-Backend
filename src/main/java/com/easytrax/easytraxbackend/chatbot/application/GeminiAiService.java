package com.easytrax.easytraxbackend.chatbot.application;

import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class GeminiAiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String modelName;
    private final ObjectMapper objectMapper;
    private final KotraApiService kotraApiService;

    public GeminiAiService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String modelName,
            @Value("${gemini.api.base-url}") String baseUrl,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            KotraApiService kotraApiService
    ) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.objectMapper = objectMapper;
        this.kotraApiService = kotraApiService;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public CompletableFuture<String> generateResponse(String userMessage, ChatbotType chatbotType) {
        return kotraApiService.searchRelevantKotraData(userMessage)
                .thenCompose(kotraData -> {
                    String systemPrompt = getSystemPrompt(chatbotType);
                    String ragPrompt = createRagPrompt(systemPrompt, kotraData, userMessage);

                    Map<String, Object> requestBody = Map.of(
                            "contents", List.of(
                                    Map.of("parts", List.of(
                                            Map.of("text", ragPrompt)
                                    ))
                            )
                    );

                    return webClient.post()
                            .uri("/v1beta/models/{model}:generateContent", modelName)
                            .header("x-goog-api-key", apiKey)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(this::extractResponseText)
                            .onErrorReturn("죄송합니다. 현재 AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.")
                            .toFuture();
                })
                .exceptionally(throwable -> {
                    log.error("RAG 처리 중 오류 발생", throwable);
                    return "죄송합니다. 현재 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
                });
    }

    private String createRagPrompt(String systemPrompt, String kotraData, String userMessage) {
        return systemPrompt + "\n\n" +
                "=== 참고할 KOTRA 공식 자료 ===\n" +
                kotraData + "\n\n" +
                "위의 KOTRA 공식 자료를 우선적으로 참고하여 답변해주세요. " +
                "KOTRA 자료에 관련 정보가 있다면 반드시 해당 정보를 기반으로 답변하고, " +
                "자료의 출처(제목, 발행일, 무역관)를 명시해주세요.\n\n" +
                "사용자 질문: " + userMessage;
    }

    private String getSystemPrompt(ChatbotType chatbotType) {
        return switch (chatbotType) {
            case COMPLIANCE_ANALYSIS -> """
                당신은 통관거부사례 분석 전문가입니다.
                KOTRA의 공식 자료를 기반으로 수출입 과정에서 발생하는 통관거부 사례들을 분석하고, 다음과 같은 정보를 제공해주세요:
                
                1. 통관거부 원인 분석 (KOTRA 자료 우선 참조)
                2. 관련 법규 및 규정 설명 (공식 출처 명시)
                3. 해결 방안 제시 (실제 사례 기반)
                4. 유사 사례 예방 방법
                
                **중요 규칙:**
                - KOTRA 공식 자료가 있으면 반드시 우선 참고하고 출처를 명시하세요
                - 추측이나 일반적인 정보보다는 실제 데이터를 기반으로 답변하세요
                - 답변 마지막에 참고한 KOTRA 자료의 제목과 발행일을 표시하세요
                
                한국어로 전문적이면서도 이해하기 쉽게 답변해주세요.
                """;
            case REGULATION_INQUIRY -> """
                당신은 수출입 규제정보 전문가입니다.
                KOTRA의 공식 자료를 기반으로 다양한 국가의 수출입 규정에 대해 다음과 같은 정보를 제공해주세요:
                
                1. 해당 국가의 수출입 규정 (KOTRA 자료 우선 참조)
                2. 라벨링 및 포장 요구사항 (공식 출처 명시)
                3. 필요한 인증 및 허가서류
                4. 검역 및 검사 절차
                5. 관세 및 세율 정보
                
                **중요 규칙:**
                - KOTRA 공식 자료가 있으면 반드시 우선 참고하고 출처를 명시하세요
                - 추측이나 일반적인 정보보다는 실제 규제 데이터를 기반으로 답변하세요
                - 답변 마지막에 참고한 KOTRA 자료의 제목과 발행일을 표시하세요
                - 가능한 경우 관련 기관의 공식 연락처나 웹사이트도 안내하세요
                
                한국어로 정확하고 최신의 정보를 제공해주세요.
                """;
        };
    }

    private String extractResponseText(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode text = parts.get(0).get("text");
                        if (text != null) {
                            return text.asText();
                        }
                    }
                }
            }
            log.warn("Gemini 응답에서 텍스트를 찾을 수 없습니다: {}", responseBody);
            return "응답을 처리하는 중 오류가 발생했습니다.";
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 중 오류 발생", e);
            return "응답을 처리하는 중 오류가 발생했습니다.";
        }
    }
}