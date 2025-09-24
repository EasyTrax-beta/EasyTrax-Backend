package com.easytrax.easytraxbackend.chatbot.application;

import com.easytrax.easytraxbackend.chatbot.api.dto.request.ChatMessageRequest;
import com.easytrax.easytraxbackend.chatbot.api.dto.response.ChatMessageResponse;
import com.easytrax.easytraxbackend.chatbot.domain.ChatMessage;
import com.easytrax.easytraxbackend.chatbot.domain.ChatSession;
import com.easytrax.easytraxbackend.chatbot.domain.MessageType;
import com.easytrax.easytraxbackend.chatbot.domain.repository.ChatMessageRepository;
import com.easytrax.easytraxbackend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionService chatSessionService;
    private final GeminiAiService geminiAiService;

    @Transactional
    public CompletableFuture<ChatMessageResponse> sendMessage(
            Long sessionId, 
            ChatMessageRequest request, 
            Long userId
    ) {
        ChatSession chatSession = chatSessionService.findChatSessionByIdAndUser(sessionId, 
                chatSessionService.findUserById(userId));
        
        ChatMessage userMessage = createUserMessage(chatSession, request.content());
        chatMessageRepository.save(userMessage);

        return geminiAiService.generateResponse(request.content(), chatSession.getChatbotType())
                .thenApply(aiResponse -> createAndSaveAiMessage(chatSession, aiResponse));
    }

    private ChatMessage createUserMessage(ChatSession chatSession, String content) {
        return ChatMessage.builder()
                .chatSession(chatSession)
                .messageType(MessageType.USER)
                .content(content)
                .build();
    }

    @Transactional
    public ChatMessageResponse createAndSaveAiMessage(ChatSession chatSession, String content) {
        ChatMessage aiMessage = ChatMessage.builder()
                .chatSession(chatSession)
                .messageType(MessageType.ASSISTANT)
                .content(content)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(aiMessage);
        updateSessionTitleIfNeeded(chatSession, content);
        
        return ChatMessageResponse.from(savedMessage);
    }

    private void updateSessionTitleIfNeeded(ChatSession chatSession, String firstAiResponse) {
        if (isDefaultTitle(chatSession.getTitle()) && chatSession.getMessages().size() <= 2) {
            String newTitle = generateTitleFromResponse(firstAiResponse);
            chatSession.updateTitle(newTitle);
        }
    }

    private boolean isDefaultTitle(String title) {
        return title != null && (
                title.endsWith(" 세션") || 
                title.equals("통관거부사례 분석") || 
                title.equals("규제정보 조회")
        );
    }

    private String generateTitleFromResponse(String response) {
        String cleanResponse = response.replaceAll("[\\n\\r]", " ").trim();
        if (cleanResponse.length() > 50) {
            return cleanResponse.substring(0, 47) + "...";
        }
        return cleanResponse;
    }
}