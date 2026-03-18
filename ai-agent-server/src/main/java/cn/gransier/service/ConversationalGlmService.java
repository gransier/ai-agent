package cn.gransier.service;

import cn.gransier.common.conversation.compressor.ContextCompressor;
import cn.gransier.common.conversation.config.ConversationProperties;
import cn.gransier.common.conversation.domain.*;
import cn.gransier.common.conversation.manager.ConversationManager;
import cn.gransier.common.conversation.manager.ConversationStore;
import cn.gransier.common.domain.ApiResult;
import cn.gransier.domain.response.GlmChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ConversationalGlmService {

    private final GlmService glmService;
    private final ConversationManager conversationManager;
    private final ConversationStore conversationStore;
    private final ContextCompressor contextCompressor;
    private final ConversationProperties properties;
    private final ObjectMapper objectMapper;

    public ConversationalGlmService(GlmService glmService, 
                                    ConversationManager conversationManager,
                                    ConversationStore conversationStore,
                                    ContextCompressor contextCompressor,
                                    ConversationProperties properties,
                                    ObjectMapper objectMapper) {
        this.glmService = glmService;
        this.conversationManager = conversationManager;
        this.conversationStore = conversationStore;
        this.contextCompressor = contextCompressor;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Flux<GlmChatResponse> chat(ChatRequest request) {
        Conversation conversation = conversationManager.getOrCreateConversation(request);
        String conversationId = conversation.getConversationId();

        log.info("Processing chat request for conversation: {}, user: {}", 
                conversationId, request.getUserId());

        List<Message> messages = conversationManager.getConversationHistory(conversationId);
        
        if (contextCompressor.estimateTotalTokens(messages) > properties.getMaxContextTokens()) {
            log.info("Context exceeds token limit, compressing...");
            messages = contextCompressor.compress(messages, (int) (properties.getMaxContextTokens() * 0.8));
        }

        List<GlmChatRequest.MessageContent> chatMessages = buildChatMessages(messages, request);

        conversationManager.addUserMessage(conversationId, request.getContent());

        GlmChatRequest glmRequest = GlmChatRequest.builder()
                .model(request.getModel() != null ? request.getModel() : "glm-4")
                .messages(chatMessages)
                .temperature(request.getTemperature() > 0 ? request.getTemperature() : 0.7)
                .maxTokens(request.getMaxTokens() > 0 ? request.getMaxTokens() : 2048)
                .stream(request.isStream())
                .userId(request.getUserId())
                .conversationId(conversationId)
                .build();

        return glmService.completions(glmRequest)
                .doOnNext(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        GlmChatResponse.Choice choice = response.getChoices().get(0);
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            log.debug("Received chunk: {}", choice.getDelta().getContent());
                        }
                    }
                })
                .doOnComplete(() -> {
                    log.info("Chat completion finished for conversation: {}", conversationId);
                    conversationManager.updateSessionActivity(conversationId);
                })
                .doOnError(error -> {
                    log.error("Chat completion error for conversation: {}", conversationId, error);
                });
    }

    public Flux<GlmChatResponse> chatWithTools(ChatRequest request, Map<String, Object> tools) {
        Conversation conversation = conversationManager.getOrCreateConversation(request);
        String conversationId = conversation.getConversationId();

        List<Message> messages = conversationManager.getConversationHistory(conversationId);
        
        if (contextCompressor.estimateTotalTokens(messages) > properties.getMaxContextTokens()) {
            messages = contextCompressor.compress(messages, (int) (properties.getMaxContextTokens() * 0.6));
        }

        List<GlmChatRequest.MessageContent> chatMessages = buildChatMessages(messages, request);

        conversationManager.addUserMessage(conversationId, request.getContent());

        GlmChatRequest glmRequest = GlmChatRequest.builder()
                .model(request.getModel() != null ? request.getModel() : "glm-4-flash")
                .messages(chatMessages)
                .temperature(request.getTemperature() > 0 ? request.getTemperature() : 0.7)
                .maxTokens(request.getMaxTokens() > 0 ? request.getMaxTokens() : 2048)
                .stream(request.isStream())
                .userId(request.getUserId())
                .conversationId(conversationId)
                .tools(tools)
                .build();

        return glmService.completions(glmRequest);
    }

    public ApiResult<List<Conversation>> getConversations(String userId) {
        List<Conversation> conversations = conversationManager.getUserConversations(userId);
        return ApiResult.success(conversations);
    }

    public ApiResult<Conversation> getConversation(String conversationId) {
        Optional<Conversation> conversation = conversationManager.getConversation(conversationId);
        return conversation.map(ApiResult::success)
                .orElse(ApiResult.error("Conversation not found"));
    }

    public ApiResult<String> deleteConversation(String conversationId) {
        conversationManager.deleteConversation(conversationId);
        return ApiResult.success("Conversation deleted");
    }

    public ApiResult<List<Message>> getMessages(String conversationId) {
        List<Message> messages = conversationManager.getConversationHistory(conversationId);
        return ApiResult.success(messages);
    }

    public ApiResult<String> clearHistory(String conversationId) {
        conversationManager.clearHistory(conversationId);
        return ApiResult.success("History cleared");
    }

    public ApiResult<String> compressContext(String conversationId) {
        Optional<Conversation> convOpt = conversationManager.getConversation(conversationId);
        if (convOpt.isEmpty()) {
            return ApiResult.error("Conversation not found");
        }

        Conversation conv = convOpt.get();
        List<Message> messages = conv.getMessages();
        List<Message> compressed = contextCompressor.compress(messages, 
                (int) (properties.getMaxContextTokens() * 0.7));

        conv.setMessages(compressed);
        conversationStore.saveConversation(conv);

        return ApiResult.success("Context compressed from " + messages.size() + " to " + compressed.size() + " messages");
    }

    private List<GlmChatRequest.MessageContent> buildChatMessages(List<Message> history, ChatRequest request) {
        List<GlmChatRequest.MessageContent> messages = new ArrayList<>();

        for (Message msg : history) {
            GlmChatRequest.MessageContent content = GlmChatRequest.MessageContent.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .name(msg.getName())
                    .build();
            messages.add(content);
        }

        messages.add(GlmChatRequest.MessageContent.builder()
                .role(Message.ROLE_USER)
                .content(request.getContent())
                .build());

        return messages;
    }
}
