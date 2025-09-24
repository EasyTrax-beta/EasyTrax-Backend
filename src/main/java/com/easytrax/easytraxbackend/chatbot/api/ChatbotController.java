package com.easytrax.easytraxbackend.chatbot.api;

import com.easytrax.easytraxbackend.chatbot.api.dto.request.ChatMessageRequest;
import com.easytrax.easytraxbackend.chatbot.api.dto.request.ChatSessionCreateRequest;
import com.easytrax.easytraxbackend.chatbot.api.dto.response.ChatMessageResponse;
import com.easytrax.easytraxbackend.chatbot.api.dto.response.ChatSessionListResponse;
import com.easytrax.easytraxbackend.chatbot.api.dto.response.ChatSessionResponse;
import com.easytrax.easytraxbackend.chatbot.application.ChatMessageService;
import com.easytrax.easytraxbackend.chatbot.application.ChatSessionService;
import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot", description = "AI 챗봇 API")
public class ChatbotController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅 세션 생성", description = "새로운 AI 채팅 세션을 생성합니다.")
    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createChatSession(
            @Valid @RequestBody ChatSessionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ChatSessionResponse response = chatSessionService.createChatSession(request, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅 세션 목록 조회", description = "사용자의 채팅 세션 목록을 조회합니다.")
    @GetMapping("/sessions")
    public ResponseEntity<Page<ChatSessionListResponse>> getChatSessions(
            @Parameter(description = "챗봇 타입 (필터링)")
            @RequestParam(required = false) ChatbotType chatbotType,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Page<ChatSessionListResponse> response = chatSessionService.findChatSessions(
                userDetails.getUserId(), 
                chatbotType, 
                pageable
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅 세션 상세 조회", description = "특정 채팅 세션의 상세 정보와 메시지를 조회합니다.")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponse> getChatSession(
            @Parameter(description = "세션 ID")
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ChatSessionResponse response = chatSessionService.findChatSession(sessionId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅 세션 삭제", description = "특정 채팅 세션을 삭제합니다.")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteChatSession(
            @Parameter(description = "세션 ID")
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        chatSessionService.deleteChatSession(sessionId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "채팅 세션 제목 수정", description = "채팅 세션의 제목을 수정합니다.")
    @PatchMapping("/sessions/{sessionId}/title")
    public ResponseEntity<ChatSessionResponse> updateSessionTitle(
            @Parameter(description = "세션 ID")
            @PathVariable Long sessionId,
            @Parameter(description = "새로운 제목")
            @RequestParam String title,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ChatSessionResponse response = chatSessionService.updateSessionTitle(
                sessionId, 
                title, 
                userDetails.getUserId()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "메시지 전송", description = "채팅 세션에 메시지를 전송하고 AI 응답을 받습니다.")
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Parameter(description = "세션 ID")
            @PathVariable Long sessionId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            ChatMessageResponse response = chatMessageService.sendMessage(sessionId, request, userDetails.getUserId()).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메시지 전송 중 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException("메시지 전송 중 오류가 발생했습니다.", e);
        }
    }
}