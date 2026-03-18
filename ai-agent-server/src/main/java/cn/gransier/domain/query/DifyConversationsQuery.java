package cn.gransier.domain.query;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class DifyConversationsQuery {

    @ApiModelProperty("用户标识")
    @NotBlank(message = "用户标识不能为空")
    private String user;

    @ApiModelProperty("（选填）当前页最后面一条记录的 ID，默认 null")
    private String last_id;

    @ApiModelProperty("（选填）一次请求返回多少条记录，默认 20 条，最大 100 条，最小 1 条。")
    private Integer limit;

    @ApiModelProperty("（选填）排序字段，默认 -updated_at(按更新时间倒序排列)，可选值：created_at, updated_at，-代表倒序")
    private String sort_by;
}
