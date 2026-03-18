package cn.gransier.controller;

import cn.gransier.common.domain.ApiResult;
import cn.gransier.common.mcp.client.McpClient;
import cn.gransier.common.mcp.config.McpProperties;
import cn.gransier.common.mcp.domain.McpToolRegistry;
import cn.gransier.common.mcp.protocol.McpTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai-cloud/mcp")
public class McpController {

    private final McpClient mcpClient;
    private final McpToolRegistry mcpToolRegistry;
    private final McpProperties mcpProperties;

    public McpController(McpClient mcpClient, McpToolRegistry mcpToolRegistry, McpProperties mcpProperties) {
        this.mcpClient = mcpClient;
        this.mcpToolRegistry = mcpToolRegistry;
        this.mcpProperties = mcpProperties;
    }

    @GetMapping("/servers")
    public ApiResult<List<Map<String, Object>>> listServers() {
        List<Map<String, Object>> servers = mcpClient.getAllConnections().stream()
                .map(conn -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", conn.getName());
                    info.put("connected", conn.isConnected());
                    info.put("initialized", conn.isInitialized());
                    info.put("protocolVersion", conn.getProtocolVersion());
                    info.put("capabilities", conn.getCapabilities());
                    try {
                        info.put("toolCount", conn.listTools().size());
                    } catch (Exception e) {
                        info.put("toolCount", 0);
                    }
                    return info;
                })
                .toList();
        return ApiResult.success(servers);
    }

    @GetMapping("/tools")
    public ApiResult<Map<String, Object>> listTools() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", mcpToolRegistry.getToolCount());
        result.put("byServer", mcpToolRegistry.getToolCountByServer());
        
        List<Map<String, Object>> tools = mcpToolRegistry.getAllTools().stream()
                .map(tool -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", tool.getName());
                    info.put("description", tool.getDescription());
                    info.put("server", tool.getServerName());
                    info.put("parameters", tool.getParameters());
                    info.put("async", tool.isAsync());
                    return info;
                })
                .toList();
        result.put("tools", tools);
        
        return ApiResult.success(result);
    }

    @PostMapping("/connect")
    public ApiResult<String> connect(@RequestBody McpProperties.ServerConfig config) {
        try {
            McpClient.McpServerConnection connection = mcpClient.connect(
                    config.getName(),
                    config.getCommand(),
                    config.getArgs().toArray(new String[0])
            );
            
            mcpToolRegistry.refreshTools();
            
            return ApiResult.success("Connected to MCP server: " + config.getName());
        } catch (Exception e) {
            log.error("Failed to connect to MCP server: {}", config.getName(), e);
            return ApiResult.error("Failed to connect: " + e.getMessage());
        }
    }

    @DeleteMapping("/disconnect/{serverName}")
    public ApiResult<String> disconnect(@PathVariable String serverName) {
        mcpToolRegistry.unregisterAllFromServer(serverName);
        mcpClient.disconnect(serverName);
        return ApiResult.success("Disconnected from MCP server: " + serverName);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/call")
    public reactor.core.publisher.Mono<ApiResult> callTool(@RequestBody Map<String, Object> request) {
        String serverName = (String) request.get("server");
        String toolName = (String) request.get("tool");
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
        
        if (serverName == null || toolName == null) {
            return reactor.core.publisher.Mono.just(ApiResult.error("Server name and tool name are required"));
        }
        
        java.util.Optional<McpClient.McpServerConnection> connOpt = mcpClient.getConnection(serverName);
        if (connOpt.isEmpty()) {
            return reactor.core.publisher.Mono.just(ApiResult.error("Server not connected: " + serverName));
        }
        
        return connOpt.get().callTool(toolName, arguments != null ? arguments : new java.util.HashMap<>())
                .<ApiResult>map(result -> ApiResult.success(new java.util.LinkedHashMap<String, Object>() {{
                    put("contentType", result.getContentType());
                    put("content", result.getContent());
                }}))
                .onErrorResume(e -> reactor.core.publisher.Mono.just(ApiResult.error("Tool call failed: " + e.getMessage())));
    }

    @PostMapping("/refresh")
    public ApiResult<String> refreshTools() {
        mcpToolRegistry.refreshTools();
        return ApiResult.success("Tools refreshed, total: " + mcpToolRegistry.getToolCount());
    }
}
