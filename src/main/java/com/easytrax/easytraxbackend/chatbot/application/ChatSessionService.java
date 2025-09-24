package com.easytrax.easytraxbackend.chatbot.application;

import com.easytrax.easytraxbackend.chatbot.api.dto.request.ChatSessionCreateRequest;
import com.easytrax.easytraxbackend.chatbot.api.dto.response.ChatSessionListResponse;
import com.easytrax.easytraxbackend.chatbot.api.dto.response.ChatSessionResponse;
import com.easytrax.easytraxbackend.chatbot.domain.ChatSession;
import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import com.easytrax.easytraxbackend.chatbot.domain.repository.ChatSessionRepository;
import com.easytrax.easytraxbackend.user.domain.User;
import com.easytrax.easytraxbackend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatSessionResponse createChatSession(ChatSessionCreateRequest request, Long userId) {
        User user = findUserById(userId);
        String sessionTitle = generateSessionTitle(request.title(), request.chatbotType());
        
        ChatSession chatSession = ChatSession.builder()
                .user(user)
                .chatbotType(request.chatbotType())
                .title(sessionTitle)
                .build();

        ChatSession savedSession = chatSessionRepository.save(chatSession);
        return ChatSessionResponse.from(savedSession);
    }

    public ChatSessionResponse findChatSession(Long sessionId, Long userId) {
        User user = findUserById(userId);
        ChatSession chatSession = findChatSessionByIdAndUser(sessionId, user);
        return ChatSessionResponse.from(chatSession);
    }

    public Page<ChatSessionListResponse> findChatSessions(Long userId, ChatbotType chatbotType, Pageable pageable) {
        User user = findUserById(userId);
        return (chatbotType != null) ?
                chatSessionRepository.findSessionListByUserAndChatbotType(user, chatbotType, pageable) :
                chatSessionRepository.findSessionListByUser(user, pageable);
    }

    @Transactional
    public void deleteChatSession(Long sessionId, Long userId) {
        User user = findUserById(userId);
        ChatSession chatSession = findChatSessionByIdAndUser(sessionId, user);
        chatSessionRepository.delete(chatSession);
    }

    @Transactional
    public ChatSessionResponse updateSessionTitle(Long sessionId, String title, Long userId) {
        User user = findUserById(userId);
        ChatSession chatSession = findChatSessionByIdAndUser(sessionId, user);
        chatSession.updateTitle(title);
        return ChatSessionResponse.from(chatSession);
    }

    public ChatSession findChatSessionByIdAndUser(Long sessionId, User user) {
        return chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다: " + sessionId));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    private String generateSessionTitle(String requestTitle, ChatbotType chatbotType) {
        if (requestTitle != null && !requestTitle.trim().isEmpty()) {
            return requestTitle.trim();
        }
        return chatbotType.getDisplayName() + " 세션";
    }
}