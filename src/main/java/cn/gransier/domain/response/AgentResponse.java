package cn.gransier.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentResponse {
    private String event;
    
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @JsonProperty("message_id")
    private String messageId;
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    @JsonProperty("task_id")
    private String taskId;
    
    private String id;
    private Integer position;
    private String thought;
    private String observation;
    private String tool;
    
    @JsonProperty("tool_labels")
    private Map<String, Object> toolLabels;
    
    @JsonProperty("tool_input")
    private String toolInput;
    
    @JsonProperty("message_files")
    private List<Object> messageFiles;
    
    private String answer;
}
