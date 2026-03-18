package cn.gransier.common.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public final class McpJsonRpc {

    private McpJsonRpc() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Request {
        private String jsonrpc = "2.0";
        private String method;
        private Object params;
        @JsonProperty("id")
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private String jsonrpc = "2.0";
        private Object result;
        private Error error;
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Error {
        private int code;
        private String message;
        private Object data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Notification {
        private String jsonrpc = "2.0";
        private String method;
        private Object params;
    }

    public static final class Methods {
        private Methods() {}
        public static final String INITIALIZE = "initialize";
        public static final String TOOLS_LIST = "tools/list";
        public static final String TOOLS_CALL = "tools/call";
        public static final String RESOURCES_LIST = "resources/list";
        public static final String RESOURCES_READ = "resources/read";
        public static final String RESOURCES_SUBSCRIBE = "resources/subscribe";
        public static final String RESOURCES_UNSUBSCRIBE = "resources/unsubscribe";
        public static final String PROMPTS_LIST = "prompts/list";
        public static final String PROMPTS_GET = "prompts/get";
        public static final String SAMPLING_CREATE = "sampling/createMessage";
        public static final String LOGGING_SET_LEVEL = "logging/setLevel";
        public static final String INITIALIZED = "initialized";
        public static final String NOTIFICATION_METHOD = "notifications/message";
    }

    public static final class ErrorCodes {
        private ErrorCodes() {}
        public static final int PARSE_ERROR = -32700;
        public static final int INVALID_REQUEST = -32600;
        public static final int METHOD_NOT_FOUND = -32601;
        public static final int INVALID_PARAMS = -32602;
        public static final int INTERNAL_ERROR = -32603;
        public static final int TOOL_NOT_FOUND = -32001;
        public static final int RESOURCE_NOT_FOUND = -32002;
    }
}
