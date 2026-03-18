package cn.gransier.common.conversation.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message implements Serializable {

    private String role;
    private String content;
    private String reasoningContent;
    private List<ToolCall> toolCalls;
    private String name;
    private Map<String, Object> metadata;
    private long timestamp;
    private boolean isCompressed;
    private String originalContent;

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_TOOL = "tool";

    public static Message user(String content) {
        return Message.builder()
                .role(ROLE_USER)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static Message assistant(String content) {
        return Message.builder()
                .role(ROLE_ASSISTANT)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static Message system(String content) {
        return Message.builder()
                .role(ROLE_SYSTEM)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static Message tool(String content, String toolCallId) {
        return Message.builder()
                .role(ROLE_TOOL)
                .content(content)
                .name(toolCallId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public int estimateTokens() {
        StringBuilder sb = new StringBuilder();
        if (role != null) sb.append(role);
        if (content != null) sb.append(content);
        if (reasoningContent != null) sb.append(reasoningContent);
        return (sb.length() / 4) + 10;
    }

    public Message compress(double ratio) {
        if (content != null && content.length() > 100) {
            this.originalContent = this.content;
            int newLength = (int) (content.length() * ratio);
            this.content = content.substring(0, newLength) + "... [已压缩]";
            this.isCompressed = true;
        }
        return this;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall implements Serializable {
        private String id;
        private String name;
        private Map<String, Object> arguments;
    }
}
