package cn.gransier.common.conversation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation implements Serializable {
    
    private String conversationId;
    private String userId;
    private String title;
    private String model;
    private List<Message> messages;
    private int messageCount;
    private int totalTokens;
    private long createdAt;
    private long updatedAt;
    private boolean archived;
    private String tags;

    public Conversation(String conversationId, String userId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.messages = new ArrayList<>();
        this.messageCount = 0;
        this.totalTokens = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public void addMessage(Message message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        this.messageCount = this.messages.size();
        this.updatedAt = System.currentTimeMillis();
    }

    public void addMessages(List<Message> newMessages) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.addAll(newMessages);
        this.messageCount = this.messages.size();
        this.updatedAt = System.currentTimeMillis();
    }

    public int estimateTokenCount() {
        int count = 0;
        for (Message msg : messages) {
            count += estimateMessageTokens(msg);
        }
        return count;
    }

    private int estimateMessageTokens(Message msg) {
        String content = "";
        if (msg.getContent() != null) {
            content += msg.getContent();
        }
        if (msg.getReasoningContent() != null) {
            content += msg.getReasoningContent();
        }
        return (content.length() / 4) + 10;
    }
}
