package cn.gransier.common.mcp.config;

import cn.gransier.common.mcp.client.McpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;

@Slf4j
@Component
public class McpClientInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final McpClient mcpClient;
    private final McpProperties mcpProperties;

    public McpClientInitializer(McpClient mcpClient, McpProperties mcpProperties) {
        this.mcpClient = mcpClient;
        this.mcpProperties = mcpProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!mcpProperties.isEnabled()) {
            log.info("MCP client is disabled");
            return;
        }

        for (McpProperties.ServerConfig serverConfig : mcpProperties.getServers()) {
            if (!serverConfig.isEnabled()) {
                log.info("MCP server '{}' is disabled, skipping", serverConfig.getName());
                continue;
            }

            try {
                log.info("Connecting to MCP server: {} (type: {})", serverConfig.getName(), serverConfig.getType());
                
                McpClient.McpServerConnection connection;
                if ("stdio".equalsIgnoreCase(serverConfig.getType())) {
                    connection = mcpClient.connect(
                            serverConfig.getName(),
                            serverConfig.getCommand(),
                            serverConfig.getArgs().toArray(new String[0])
                    );
                } else if ("http".equalsIgnoreCase(serverConfig.getType()) || "sse".equalsIgnoreCase(serverConfig.getType())) {
                    log.warn("HTTP/SSE transport not fully implemented for server: {}", serverConfig.getName());
                    continue;
                } else {
                    log.warn("Unknown MCP transport type: {} for server: {}", serverConfig.getType(), serverConfig.getName());
                    continue;
                }

                log.info("Connected to MCP server: {}, available tools: {}", 
                        serverConfig.getName(), connection.listTools().size());

            } catch (Exception e) {
                log.error("Failed to connect to MCP server: {}", serverConfig.getName(), e);
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Closing all MCP connections");
        for (McpClient.McpServerConnection connection : mcpClient.getAllConnections()) {
            try {
                connection.close();
            } catch (Exception e) {
                log.warn("Error closing MCP connection: {}", connection.getName(), e);
            }
        }
    }
}
