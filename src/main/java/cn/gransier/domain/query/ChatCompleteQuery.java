package cn.gransier.domain.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChatCompleteQuery {

    @ApiModelProperty(value = "会话id")
    private String chatId;
    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "是否流式传输")
    private boolean stream = true;

    @ApiModelProperty(value = "是否展示详情")
    private boolean detail = false;

    @ApiModelProperty(value = "聊天内容")
    private String content;
}
