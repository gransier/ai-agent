package cn.gransier.common.conversation.manager;

import cn.gransier.common.conversation.domain.Conversation;
import cn.gransier.common.conversation.domain.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryConversationStore implements ConversationStore {

    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private final Map<String, String> userConversations = new ConcurrentHashMap<>();

    @Override
    public Conversation createConversation(String userId, String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = generateConversationId();
        }
        Conversation conversation = new Conversation(conversationId, userId);
        conversations.put(conversationId, conversation);
        userConversations.put(conversationId, userId);
        log.info("Created conversation: {} for user: {}", conversationId, userId);
        return conversation;
    }

    @Override
    public Optional<Conversation> getConversation(String conversationId) {
        return Optional.ofNullable(conversations.get(conversationId));
    }

    @Override
    public List<Conversation> getUserConversations(String userId) {
        return conversations.values().stream()
                .filter(c -> userId.equals(c.getUserId()) && !c.isArchived())
                .sorted((a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void saveConversation(Conversation conversation) {
        if (conversation != null && conversation.getConversationId() != null) {
            conversations.put(conversation.getConversationId(), conversation);
            log.debug("Saved conversation: {}", conversation.getConversationId());
        }
    }

    @Override
    public void deleteConversation(String conversationId) {
        Conversation removed = conversations.remove(conversationId);
        if (removed != null) {
            userConversations.remove(conversationId);
            log.info("Deleted conversation: {}", conversationId);
        }
    }

    @Override
    public void archiveConversation(String conversationId) {
        getConversation(conversationId).ifPresent(conv -> {
            conv.setArchived(true);
            saveConversation(conv);
            log.info("Archived conversation: {}", conversationId);
        });
    }

    @Override
    public void addMessage(String conversationId, Message message) {
        getConversation(conversationId).ifPresent(conv -> {
            conv.addMessage(message);
            saveConversation(conv);
            log.debug("Added message to conversation: {}", conversationId);
        });
    }

    @Override
    public List<Message> getMessages(String conversationId) {
        return getConversation(conversationId)
                .map(Conversation::getMessages)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Message> getRecentMessages(String conversationId, int count) {
        List<Message> messages = getMessages(conversationId);
        if (messages.size() <= count) {
            return messages;
        }
        return messages.subList(messages.size() - count, messages.size());
    }

    @Override
    public void clearMessages(String conversationId) {
        getConversation(conversationId).ifPresent(conv -> {
            conv.setMessages(new ArrayList<>());
            conv.setMessageCount(0);
            conv.setTotalTokens(0);
            saveConversation(conv);
            log.info("Cleared messages from conversation: {}", conversationId);
        });
    }

    @Override
    public int getConversationCount() {
        return conversations.size();
    }

    private String generateConversationId() {
        return "conv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
