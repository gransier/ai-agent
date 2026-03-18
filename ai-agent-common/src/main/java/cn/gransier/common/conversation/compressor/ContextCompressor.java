package cn.gransier.common.conversation.compressor;

import cn.gransier.common.conversation.domain.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ContextCompressor {

    public List<Message> compress(List<Message> messages, int targetTokenCount) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        int currentTokenCount = estimateTotalTokens(messages);
        
        if (currentTokenCount <= targetTokenCount) {
            return new ArrayList<>(messages);
        }

        log.info("Compressing context from {} tokens to {} tokens", currentTokenCount, targetTokenCount);

        List<Message> compressed = new ArrayList<>();
        
        List<Message> systemMessages = messages.stream()
                .filter(m -> Message.ROLE_SYSTEM.equals(m.getRole()))
                .toList();
        compressed.addAll(systemMessages);

        List<Message> otherMessages = messages.stream()
                .filter(m -> !Message.ROLE_SYSTEM.equals(m.getRole()))
                .toList();

        if (otherMessages.size() <= 2) {
            compressed.addAll(otherMessages);
            return compressed;
        }

        int reservedTokens = estimateTokenCount(systemMessages) + 500;
        int availableTokens = targetTokenCount - reservedTokens;

        StringBuilder summary = new StringBuilder();
        summary.append("[对话摘要] 早期对话内容已压缩:\n");

        List<Message> importantMessages = new ArrayList<>();
        importantMessages.add(otherMessages.get(0));
        
        int tokensUsed = estimateTokenCount(importantMessages);
        
        for (int i = otherMessages.size() - 1; i >= 1 && tokensUsed < availableTokens; i--) {
            Message msg = otherMessages.get(i);
            int msgTokens = msg.estimateTokens();
            if (tokensUsed + msgTokens <= availableTokens) {
                importantMessages.add(msg);
                tokensUsed += msgTokens;
            } else {
                String content = msg.getContent();
                if (content != null && content.length() > 50) {
                    summary.append("- ").append(content, 0, Math.min(50, content.length())).append("...\n");
                }
            }
        }

        for (int i = importantMessages.size() - 1; i >= 0; i--) {
            compressed.add(importantMessages.get(i));
        }

        Message summaryMessage = Message.builder()
                .role(Message.ROLE_SYSTEM)
                .content(summary.toString())
                .isCompressed(true)
                .timestamp(System.currentTimeMillis())
                .build();
        compressed.add(1, summaryMessage);

        log.info("Compression complete: {} messages reduced to {} messages", 
                messages.size(), compressed.size());

        return compressed;
    }

    public List<Message> compressMiddle(List<Message> messages, int targetTokenCount) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        int currentTokenCount = estimateTotalTokens(messages);
        
        if (currentTokenCount <= targetTokenCount) {
            return new ArrayList<>(messages);
        }

        List<Message> systemMessages = messages.stream()
                .filter(m -> Message.ROLE_SYSTEM.equals(m.getRole()))
                .toList();
        
        List<Message> nonSystemMessages = messages.stream()
                .filter(m -> !Message.ROLE_SYSTEM.equals(m.getRole()))
                .toList();

        if (nonSystemMessages.size() <= 4) {
            return new ArrayList<>(messages);
        }

        List<Message> result = new ArrayList<>(systemMessages);
        
        result.add(nonSystemMessages.get(0));
        
        Message compressionNote = Message.builder()
                .role(Message.ROLE_SYSTEM)
                .content("[此前的 " + (nonSystemMessages.size() - 2) + " 条对话已省略]")
                .isCompressed(true)
                .timestamp(System.currentTimeMillis())
                .build();
        result.add(compressionNote);
        
        result.add(nonSystemMessages.get(nonSystemMessages.size() - 1));

        return result;
    }

    public int estimateTotalTokens(List<Message> messages) {
        return messages.stream()
                .mapToInt(Message::estimateTokens)
                .sum();
    }

    private int estimateTokenCount(List<Message> messages) {
        return messages.stream()
                .mapToInt(Message::estimateTokens)
                .sum();
    }
}
