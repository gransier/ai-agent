package cn.gransier.common.conversation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest implements Serializable {

    private String userId;
    private String conversationId;
    private String content;
    private String model;
    private double temperature;
    private int maxTokens;
    private boolean stream;
    private String systemPrompt;
    private Map<String, Object> metadata;
    private String requestId;

    public static ChatRequest of(String userId, String content) {
        return ChatRequest.builder()
                .userId(userId)
                .content(content)
                .stream(true)
                .temperature(0.7)
                .maxTokens(2048)
                .build();
    }

    public static ChatRequest of(String userId, String conversationId, String content) {
        return ChatRequest.builder()
                .userId(userId)
                .conversationId(conversationId)
                .content(content)
                .stream(true)
                .temperature(0.7)
                .maxTokens(2048)
                .build();
    }
}
