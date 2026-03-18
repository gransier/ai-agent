package cn.gransier.common.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    private boolean enabled = true;
    private List<ServerConfig> servers = new ArrayList<>();
    private int connectionTimeout = 30;
    private int requestTimeout = 60;

    @Data
    public static class ServerConfig {
        private String name;
        private String type = "stdio";
        private String command;
        private List<String> args = new java.util.ArrayList<>();
        private String url;
        private String apiKey;
        private Map<String, String> env = new java.util.HashMap<>();
        private boolean enabled = true;
    }
}
