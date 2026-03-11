package cn.gransier.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "chatQuery", description = "chatQuery")
public class ChatQuery {

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    private String appId;


    /**
     * 聊天id
     */
    @ApiModelProperty(value = "聊天id")
    private String chatId;


}
