package cn.gransier.common.domain;

import lombok.Getter;

@Getter
public enum ResultEnum {
    SUCCESS("00000", "成功"),
    ERROR("999", "错误"),
    PARAMETER_EXCEPTION("201", "参数校验失败"),
    REQUEST_ILLEGAL("202", "请求非法"),
    ACCESS_LIMIT_REACHED("203", "访问太频繁！"),
    BUSINESS_ERROR("204", "业务操作失败!"),
    NO_PERMISSION("205", "没此权限，请联系管理员！"),
    BAD_CREDENTIALS_EXPIRED("A0014", "用户认证异常"),
    TOKEN_PAST("301", "token过期"),
    TOKEN_ERROR("302", "token异常"),
    LOGIN_ERROR("303", "登录异常"),
    REMOTE_ERROR("304", "异地登录"),
    MENU_PAST("305", "菜单过期"),
    SESSION_ERROR("500210", "Session不存在或者已经失效"),
    PASSWORD_EMPTY("500211", "登录密码不能为空"),
    MOBILE_EMPTY("500212", "手机号不能为空"),
    MOBILE_ERROR("500213", "手机号格式错误"),
    MOBILE_NOT_EXIST("500214", "手机号不存在"),
    PASSWORD_ERROR("500215", "密码错误");

    private final String code;
    private final String desc;

    ResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

