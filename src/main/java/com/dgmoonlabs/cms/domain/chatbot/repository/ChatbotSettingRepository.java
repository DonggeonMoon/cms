package com.dgmoonlabs.cms.domain.chatbot.repository;

import com.dgmoonlabs.cms.domain.chatbot.entity.ChatbotSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatbotSettingRepository extends JpaRepository<ChatbotSetting, Long> {
    Optional<ChatbotSetting> findFirstByOrderByIdAsc();
}
