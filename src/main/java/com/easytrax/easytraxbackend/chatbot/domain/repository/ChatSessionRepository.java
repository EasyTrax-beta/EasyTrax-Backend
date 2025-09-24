package com.easytrax.easytraxbackend.chatbot.domain.repository;

import com.easytrax.easytraxbackend.chatbot.domain.ChatSession;
import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import com.easytrax.easytraxbackend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<ChatSession> findByIdAndUser(Long id, User user);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user ORDER BY cs.updatedAt DESC")
    Page<ChatSession> findByUserOrderByUpdatedAtDesc(@Param("user") User user, Pageable pageable);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user AND cs.chatbotType = :chatbotType ORDER BY cs.updatedAt DESC")
    Page<ChatSession> findByUserAndChatbotTypeOrderByUpdatedAtDesc(
            @Param("user") User user, 
            @Param("chatbotType") ChatbotType chatbotType, 
            Pageable pageable
    );
}