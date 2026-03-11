package cn.gransier.domain.query;

import lombok.Data;

import java.util.Map;

@Data
public class AgentQuery {

    private Map<String, Object> inputs = Map.of();
    private String query;
    // streaming:流式 blocking：阻塞
    private String response_mode;
    // 用户id
    private String user;
    // 会话id
    private String conversation_id;

    public AgentQuery(String query, String user, String conversation_id) {
        this.query = query;
        this.user = user;
        this.conversation_id = conversation_id;
        this.response_mode = "streaming";
    }

    public AgentQuery(String query, String user, String conversation_id, boolean stream) {
        this.query = query;
        this.user = user;
        this.conversation_id = conversation_id;
        if (stream) {
            this.response_mode = "streaming";
        } else {
            this.response_mode = "blocking";
        }
    }

    public AgentQuery(Map<String, Object> inputs, String query, boolean stream, String user, String conversation_id) {
        this.inputs = inputs;
        this.query = query;
        if (stream) {
            this.response_mode = "streaming";
        } else {
            this.response_mode = "blocking";
        }
        this.user = user;
        this.conversation_id = conversation_id;
    }
}
