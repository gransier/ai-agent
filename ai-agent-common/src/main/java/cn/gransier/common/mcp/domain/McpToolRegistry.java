package cn.gransier.common.mcp.domain;

import cn.gransier.common.mcp.client.McpClient;
import cn.gransier.common.mcp.protocol.McpTypes;
import cn.gransier.common.tool.domain.BaseTool;
import cn.gransier.common.tool.registry.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class McpToolRegistry {

    private final McpClient mcpClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final Map<String, McpToolAdapter> mcpTools = new ConcurrentHashMap<>();

    public McpToolRegistry(McpClient mcpClient, ToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.mcpClient = mcpClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        refreshTools();
    }

    public void refreshTools() {
        Map<String, List<McpTypes.Tool>> allMcpTools = mcpClient.getAllTools();
        
        for (Map.Entry<String, List<McpTypes.Tool>> entry : allMcpTools.entrySet()) {
            String serverName = entry.getKey();
            List<McpTypes.Tool> tools = entry.getValue();
            
            for (McpTypes.Tool tool : tools) {
                String toolName = serverName + ":" + tool.getName();
                
                if (!mcpTools.containsKey(toolName)) {
                    mcpClient.getConnection(serverName).ifPresent(connection -> {
                        McpToolAdapter adapter = new McpToolAdapter(serverName, tool, connection, objectMapper);
                        mcpTools.put(toolName, adapter);
                        toolRegistry.register(toolName, adapter);
                        log.info("Registered MCP tool: {} from server: {}", tool.getName(), serverName);
                    });
                }
            }
        }
    }

    public McpToolAdapter getTool(String serverName, String toolName) {
        return mcpTools.get(serverName + ":" + toolName);
    }

    public List<McpToolAdapter> getAllTools() {
        return List.copyOf(mcpTools.values());
    }

    public int getToolCount() {
        return mcpTools.size();
    }

    public void unregisterTool(String serverName, String toolName) {
        String fullName = serverName + ":" + toolName;
        mcpTools.remove(fullName);
        toolRegistry.unregister(fullName);
        log.info("Unregistered MCP tool: {}", fullName);
    }

    public void unregisterAllFromServer(String serverName) {
        mcpTools.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(serverName + ":")) {
                toolRegistry.unregister(entry.getKey());
                return true;
            }
            return false;
        });
        log.info("Unregistered all MCP tools from server: {}", serverName);
    }

    public Map<String, Integer> getToolCountByServer() {
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        for (McpToolAdapter tool : mcpTools.values()) {
            counts.merge(tool.getServerName(), 1, Integer::sum);
        }
        return counts;
    }
}
