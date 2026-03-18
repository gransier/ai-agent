package cn.gransier.common.conversation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlmChatRequest {

    private String model;
    private List<MessageContent> messages;
    private double temperature;
    private int topP;
    private int maxTokens;
    private boolean stream;
    private String userId;
    private String conversationId;
    private Map<String, Object> tools;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageContent {
        private String role;
        private String content;
        private String name;
        private List<ToolCall> toolCalls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Function {
        private String name;
        private String arguments;
    }
}
