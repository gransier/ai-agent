package cn.gransier.domain.response;

import lombok.Data;

@Data
public class DifyErrorResponse {
    private String code;
    private String message;
    private int status;
}
