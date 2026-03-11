package cn.gransier.domain.response;

import lombok.Data;

@Data
public class AgentResponse {
    private String event;
    private String conversationId;
    private String messageId;
    private Long createdAt;
    private String taskId;
    private String id;
    private String answer;
}
