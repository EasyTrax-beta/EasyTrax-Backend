package com.easytrax.easytraxbackend.chatbot.domain.repository;

import com.easytrax.easytraxbackend.chatbot.domain.ChatMessage;
import com.easytrax.easytraxbackend.chatbot.domain.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(@Param("chatSession") ChatSession chatSession);
}