package cn.gransier.common.conversation.manager;

import cn.gransier.common.conversation.domain.ChatRequest;
import cn.gransier.common.conversation.domain.Conversation;
import cn.gransier.common.conversation.domain.Message;
import cn.gransier.common.conversation.config.ConversationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConversationManager {

    private final ConversationStore store;
    private final ConversationProperties properties;
    private final Map<String, SessionContext> activeSessions = new ConcurrentHashMap<>();

    public ConversationManager(ConversationStore store, ConversationProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    public Conversation getOrCreateConversation(ChatRequest request) {
        if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
            return store.getConversation(request.getConversationId())
                    .orElseGet(() -> store.createConversation(request.getUserId(), request.getConversationId()));
        }
        return store.createConversation(request.getUserId(), null);
    }

    public Optional<Conversation> getConversation(String conversationId) {
        return store.getConversation(conversationId);
    }

    public List<Conversation> getUserConversations(String userId) {
        return store.getUserConversations(userId);
    }

    public void deleteConversation(String conversationId) {
        store.deleteConversation(conversationId);
        activeSessions.remove(conversationId);
    }

    public void addUserMessage(String conversationId, String content) {
        store.addMessage(conversationId, Message.user(content));
    }

    public void addAssistantMessage(String conversationId, String content) {
        store.addMessage(conversationId, Message.assistant(content));
    }

    public void addAssistantMessage(String conversationId, String content, String reasoningContent) {
        Message message = Message.assistant(content);
        message.setReasoningContent(reasoningContent);
        store.addMessage(conversationId, message);
    }

    public void addSystemMessage(String conversationId, String content) {
        store.addMessage(conversationId, Message.system(content));
    }

    public List<Message> getConversationHistory(String conversationId) {
        return store.getMessages(conversationId);
    }

    public List<Message> getRecentHistory(String conversationId, int messageCount) {
        return store.getRecentMessages(conversationId, messageCount);
    }

    public int getTokenCount(String conversationId) {
        return store.getConversation(conversationId)
                .map(Conversation::estimateTokenCount)
                .orElse(0);
    }

    public boolean needsCompression(String conversationId) {
        int tokenCount = getTokenCount(conversationId);
        return tokenCount > properties.getMaxContextTokens();
    }

    public void clearHistory(String conversationId) {
        store.clearMessages(conversationId);
        activeSessions.remove(conversationId);
    }

    public SessionContext getActiveSession(String conversationId) {
        return activeSessions.computeIfAbsent(conversationId, 
            id -> new SessionContext(id, properties.getSessionTimeout()));
    }

    public void updateSessionActivity(String conversationId) {
        getActiveSession(conversationId).updateLastAccess();
    }

    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(now));
        log.debug("Cleaned up expired sessions, active count: {}", activeSessions.size());
    }

    public List<Message> buildMessagesForRequest(String conversationId, String currentContent, String systemPrompt) {
        List<Message> messages = new ArrayList<>();
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Message.system(systemPrompt));
        }
        
        List<Message> history = getConversationHistory(conversationId);
        messages.addAll(history);
        
        messages.add(Message.user(currentContent));
        
        return messages;
    }

    public static class SessionContext {
        private final String conversationId;
        private final long timeoutMs;
        private volatile long lastAccessTime;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();

        public SessionContext(String conversationId, long sessionTimeout) {
            this.conversationId = conversationId;
            this.timeoutMs = sessionTimeout * 1000;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        public boolean isExpired(long now) {
            return (now - lastAccessTime) > timeoutMs;
        }

        public String getConversationId() {
            return conversationId;
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
    }
}
