package cn.gransier.common.conversation.manager;

import cn.gransier.common.conversation.domain.Conversation;
import cn.gransier.common.conversation.domain.Message;

import java.util.List;
import java.util.Optional;

public interface ConversationStore {

    Conversation createConversation(String userId, String conversationId);

    Optional<Conversation> getConversation(String conversationId);

    List<Conversation> getUserConversations(String userId);

    void saveConversation(Conversation conversation);

    void deleteConversation(String conversationId);

    void archiveConversation(String conversationId);

    void addMessage(String conversationId, Message message);

    List<Message> getMessages(String conversationId);

    List<Message> getRecentMessages(String conversationId, int count);

    void clearMessages(String conversationId);

    int getConversationCount();
}
