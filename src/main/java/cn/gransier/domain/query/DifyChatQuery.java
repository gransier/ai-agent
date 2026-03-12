package cn.gransier.domain.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@SuppressWarnings("unused")
public class DifyChatQuery {

    @ApiModelProperty("允许传入 App 定义的各变量值。 inputs 参数包含了多组键值对（Key/Value pairs），每组的键对应一个特定变量，每组的值则是该变量的具体值。 默认 {}")
    private Map<String, Object> inputs = Map.of();
    @ApiModelProperty("用户输入/提问内容")
    private String query;
    @ApiModelProperty("streaming:流式 blocking：阻塞")
    private String response_mode;
    @ApiModelProperty("用户标识")
    private String user;
    @ApiModelProperty("（选填）会话 ID，需要基于之前的聊天记录继续对话，必须传之前消息的 conversation_id。")
    private String conversation_id;

    public DifyChatQuery() {
    }

    public DifyChatQuery(String query, String user, String conversation_id) {
        this.query = query;
        this.user = user;
        this.conversation_id = conversation_id;
        this.response_mode = "streaming";
    }

    public DifyChatQuery(String query, String user, String conversation_id, boolean stream) {
        this.query = query;
        this.user = user;
        this.conversation_id = conversation_id;
        if (stream) {
            this.response_mode = "streaming";
        } else {
            this.response_mode = "blocking";
        }
    }

    public DifyChatQuery(Map<String, Object> inputs, String query, boolean stream, String user, String conversation_id) {
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
