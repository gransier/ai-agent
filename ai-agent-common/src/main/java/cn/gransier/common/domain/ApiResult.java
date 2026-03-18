package cn.gransier.common.domain;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
@Getter
public class ApiResult<T> implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ApiResult.class);
    @Serial
    private static final long serialVersionUID = 1L;
    private String code = "500";
    private String message = null;
    private T data = null;

    public ApiResult() {
    }

    public ApiResult(String code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public static <T> ApiResult<T> result(ResultEnum resultEnum) {
        return result(resultEnum, null);
    }

    public static <T> ApiResult<T> result(ResultEnum resultEnum, T data) {
        return result(resultEnum, null, data);
    }

    public static <T> ApiResult<T> result(ResultEnum resultEnum, String message, T data) {
        return new ApiResult<>(resultEnum.getCode(), message, data);
    }

    public static <T> ApiResult<T> error() {
        logger.debug("返回错误：code={}, msg={}", ResultEnum.ERROR.getCode(), ResultEnum.ERROR.getDesc());
        return new ApiResult<>(ResultEnum.ERROR.getCode(), ResultEnum.ERROR.getDesc(), null);
    }

    public static <T> ApiResult<T> error(String msg) {
        logger.debug("返回错误：code={}, msg={}", ResultEnum.ERROR.getCode(), msg);
        return new ApiResult<>(ResultEnum.ERROR.getCode(), msg, null);
    }

    public static <T> ApiResult<T> error(ResultEnum resultEnum) {
        logger.debug("返回错误：code={}, msg={}", resultEnum.getCode(), resultEnum.getDesc());
        return new ApiResult<>(resultEnum.getCode(), resultEnum.getDesc(), null);
    }

    public static <T> ApiResult<T> error(String code, String msg) {
        logger.debug("返回错误：code={}, msg={}", code, msg);
        return new ApiResult<>(code, msg, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ResultEnum.SUCCESS.getCode(), null, data);
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getDesc(), null);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toString() {
        return "Result [code=" + this.code + ", message=" + this.message + ", data=" + this.data + "]";
    }
}
