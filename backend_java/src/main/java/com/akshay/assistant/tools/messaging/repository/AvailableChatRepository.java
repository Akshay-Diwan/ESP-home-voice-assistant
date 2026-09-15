package com.akshay.assistant.tools.messaging.repository;

import com.akshay.assistant.tools.messaging.entity.AvailableChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvailableChatRepository
        extends JpaRepository<AvailableChatEntity, Long> {

    Optional<AvailableChatEntity> findByChannelId(String channelId);

    Optional<AvailableChatEntity> findByAuthorId(String authorId);

    Optional<AvailableChatEntity> findByName(String name);

    Optional<AvailableChatEntity> findByGlobalName(String globalName);

    List<AvailableChatEntity> findAllByNameIgnoreCase(String name);
}