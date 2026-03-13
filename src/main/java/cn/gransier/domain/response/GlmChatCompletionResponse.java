package cn.gransier.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 智谱 AI (GLM) Chat Completion 响应根对象
 */
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

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }

    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    /**
     * Choices 列表项
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private Message message;

        @JsonProperty("finish_reason")
        private String finishReason;

        // Getters and Setters
        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }

        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }

    /**
     * 消息内容对象
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;

        // 注意：reasoning_content 是智谱/GLM 特有的字段，用于返回思维链过程
        @JsonProperty("reasoning_content")
        private String reasoningContent;

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getReasoningContent() { return reasoningContent; }
        public void setReasoningContent(String reasoningContent) { this.reasoningContent = reasoningContent; }
    }

    /**
     * Token 使用统计
     */
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

        // Getters and Setters
        public Integer getPromptTokens() { return promptTokens; }
        public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

        public Integer getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

        public Integer getTotalTokens() { return totalTokens; }
        public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }

        public TokenDetails getPromptTokensDetails() { return promptTokensDetails; }
        public void setPromptTokensDetails(TokenDetails promptTokensDetails) { this.promptTokensDetails = promptTokensDetails; }

        public CompletionTokenDetails getCompletionTokensDetails() { return completionTokensDetails; }
        public void setCompletionTokensDetails(CompletionTokenDetails completionTokensDetails) { this.completionTokensDetails = completionTokensDetails; }
    }

    /**
     * Prompt Token 详情
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenDetails {
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;

        public Integer getCachedTokens() { return cachedTokens; }
        public void setCachedTokens(Integer cachedTokens) { this.cachedTokens = cachedTokens; }
    }

    /**
     * Completion Token 详情
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompletionTokenDetails {
        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;

        public Integer getReasoningTokens() { return reasoningTokens; }
        public void setReasoningTokens(Integer reasoningTokens) { this.reasoningTokens = reasoningTokens; }
    }
}
