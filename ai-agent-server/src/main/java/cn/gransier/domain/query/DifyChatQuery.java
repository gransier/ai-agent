package cn.gransier.domain.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@SuppressWarnings("unused")
public class DifyChatQuery {

    @ApiModelProperty("允许传入 App 定义的各变量值。 inputs 参数包含了多组键值对（Key/Value pairs），每组的键对应一个特定变量，每组的值则是该变量的具体值。 默认 {}")
    @JsonInclude
    private Map<String, Object> inputs = Map.of();
    @ApiModelProperty("用户输入/提问内容")
    private String query;

    @ApiModelProperty("streaming:流式 blocking：阻塞")
    private String response_mode;
    @ApiModelProperty("用户标识")
    private String user;
    @ApiModelProperty("（选填）会话 ID，需要基于之前的聊天记录继续对话，必须传之前消息的 conversation_id。")
    private String conversation_id;

    @ApiModelProperty(value = "文件列表")
    private List<File> files;


    @Data
    public static class File {

        private String type;

        private String transferMethod;

        private String uploadFileId;

        private String url;
    }
}
