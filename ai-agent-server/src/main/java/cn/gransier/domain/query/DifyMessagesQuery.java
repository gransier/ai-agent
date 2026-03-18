package cn.gransier.domain.query;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DifyMessagesQuery {

    @ApiModelProperty("会话 ID")
    @NotBlank(message = "会话 ID 不能为空")
    private String conversation_id;

    @NotBlank(message = "用户标识不能为空")
    @ApiModelProperty("用户标识")
    private String user;

    @ApiModelProperty("（选填）当前页最后面一条记录的 ID，默认 null")
    private String last_id;

    @ApiModelProperty("（选填）一次请求返回多少条记录，默认 20 条，最大 100 条，最小 1 条。")
    private Integer limit = 20;
}
