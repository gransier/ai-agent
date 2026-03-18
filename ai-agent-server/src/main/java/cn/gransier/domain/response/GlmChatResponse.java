package cn.gransier.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 智谱 AI (GLM) Chat Completion 响应根对象
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中多余未知的字段，防止报错
public class GlmChatResponse {

    private String id;

    private Long created;

    private String object;

    private String model;

    private List<Choice> choices;

    // 如果有 usage 字段也可以加在这里，通常流式最后一条才有
    private Usage usage;

    /**
     * 内部类：Choices 数组元素
     */
    @Data
    public static class Choice {

        private Integer index;

        private Delta delta;

        // 流式结束时可能有 finish_reason
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * 内部类：Delta 增量内容 (核心数据在这里)
     */
    @Data
    public static class Delta {

        private String role;

        private String content;

        // 【重要】智谱特有的思维链内容 (reasoning_content)
        @JsonProperty("reasoning_content")
        private String reasoningContent;

        // 其他可能的字段，如 tool_calls 等
        @JsonProperty("tool_calls")
        private List<Object> toolCalls;
    }

    /**
     * 内部类：Usage (通常在最后一个 chunk 出现)
     */
    @Data
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    private Errors error;

    /**
     * 异常信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Errors {
        @JsonProperty("code")
        private String code;
        @JsonProperty("message")
        private String message;

    }
}
