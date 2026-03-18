package cn.gransier.domain.query;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatCompleteQuery {
    @ApiModelProperty(value = "会话id")
    private String conversation_id;

    @ApiModelProperty(value = "用户id")
    @NotBlank(message = "用户id不能为空")
    private String user;

    @ApiModelProperty(value = "是否流式传输，默认true")
    private boolean stream = true;

    @ApiModelProperty(value = "聊天内容")
    @NotBlank(message = "聊天内容不能为空")
    private String content;
}
