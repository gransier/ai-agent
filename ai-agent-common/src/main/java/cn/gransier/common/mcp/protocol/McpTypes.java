package cn.gransier.common.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public interface McpTypes {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class InitializeResult {
        private String protocolVersion;
        private ServerInfo serverInfo;
        private Capabilities capabilities;
        private Instructions instructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ServerInfo {
        private String name;
        private String version;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Capabilities {
        private boolean listTools;
        private boolean callTools;
        private boolean listResources;
        private boolean readResources;
        private boolean subscribeResources;
        private boolean listPrompts;
        private boolean getPrompts;
        private boolean sampling;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Instructions {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Tool {
        private String name;
        private String description;
        private Map<String, ParamSchema> inputSchema;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ParamSchema {
        private String type;
        private String description;
        private boolean required;
        private Object defaultValue;
        private List<String> enumValues;
        private Map<String, ParamSchema> properties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ToolCallResult {
        private String contentType;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Resource {
        private String uri;
        private String name;
        private String description;
        private String mimeType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ResourceContent {
        private String uri;
        private String mimeType;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Prompt {
        private String name;
        private String description;
        private List<PromptArgument> arguments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PromptArgument {
        private String name;
        private String description;
        private boolean required;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ListResponse<T> {
        private List<T> tools;
        private List<T> resources;
        private List<T> prompts;
    }
}
