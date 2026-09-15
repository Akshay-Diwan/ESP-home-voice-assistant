package com.akshay.assistant.tools.messaging.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "available_chats")
public class AvailableChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String authorId;

    @Column(nullable = false)
    private String channelId;

    private String name;

    private String globalName;

    public AvailableChatEntity() {
    }

    public AvailableChatEntity(
            String authorId,
            String channelId,
            String name,
            String globalName
    ) {
        this.authorId = authorId;
        this.channelId = channelId;
        this.name = name;
        this.globalName = globalName;
    }

    public Long getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGlobalName() {
        return globalName;
    }

    public void setGlobalName(String globalName) {
        this.globalName = globalName;
    }
}