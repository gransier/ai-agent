package cn.gransier.common.conversation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "conversation")
public class ConversationProperties {

    private boolean enabled = true;
    private int maxContextTokens = 128000;
    private int maxMessages = 100;
    private int compressionThreshold = 100000;
    private double compressionRatio = 0.3;
    private boolean autoTitle = true;
    private boolean persistEnabled = false;
    private int sessionTimeoutMinutes = 60;
    private int maxSessionsPerUser = 50;
    private String storageType = "memory";
    private long sessionTimeout = 3600;
}
