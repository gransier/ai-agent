package cn.gransier.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DifyUploadFileResponse {

    @ApiModelProperty(value = "文件id")
    private String id;


    @ApiModelProperty(value = "文件名")
    private String name;


    @ApiModelProperty(value = "文件大小")
    private Long size;


    @ApiModelProperty(value = "文件后缀")
    private String extension;


    @ApiModelProperty(value = "文件 mime-type,e.g:text/plain")
    private String mimeType;


    @ApiModelProperty(value = "创建者")
    private String createdBy;


    @ApiModelProperty(value = "创建时间")
    private Long createdAt;


}
