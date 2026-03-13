package cn.gransier.domain.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DifyChatResponse {
    private String event;
    
    private String conversation_id;
    
    private String message_id;
    
    private Long created_at;
    
    private String task_id;
    
    private String id;
    private Integer position;
    private String thought;
    private String observation;
    private String tool;
    
    private Map<String, Object> tool_labels;
    
    private String tool_input;
    
    private List<Object> message_files;
    
    private String answer;

    // 异常信息
    private String code;
    private String message;
    private int status;
}
