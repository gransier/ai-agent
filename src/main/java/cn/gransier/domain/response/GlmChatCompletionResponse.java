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
public class GlmChatCompletionResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("model")
    private String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("usage")
    private Usage usage;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("error")
    private Errors error;

    /**
     * Choices 列表项
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private Message message;

        @JsonProperty("finish_reason")
        private String finishReason;

    }

    /**
     * 消息内容对象
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;

        // 注意：reasoning_content 是智谱/GLM 特有的字段，用于返回思维链过程
        @JsonProperty("reasoning_content")
        private String reasoningContent;

    }

    /**
     * Token 使用统计
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;

        // 嵌套的详细信息对象
        @JsonProperty("prompt_tokens_details")
        private TokenDetails promptTokensDetails;

        @JsonProperty("completion_tokens_details")
        private CompletionTokenDetails completionTokensDetails;

    }

    /**
     * Prompt Token 详情
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenDetails {
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;

    }

    /**
     * Completion Token 详情
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompletionTokenDetails {
        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;

    }

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
