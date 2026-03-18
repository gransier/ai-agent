package cn.gransier.common.mcp.domain;

import cn.gransier.common.tool.domain.BaseTool;
import cn.gransier.common.mcp.client.McpClient;
import cn.gransier.common.mcp.protocol.McpTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class McpToolAdapter implements BaseTool {

    private final String serverName;
    private final McpTypes.Tool mcpTool;
    private final McpClient.McpServerConnection connection;
    private final ObjectMapper objectMapper;

    public McpToolAdapter(String serverName, McpTypes.Tool mcpTool, 
                          McpClient.McpServerConnection connection, ObjectMapper objectMapper) {
        this.serverName = serverName;
        this.mcpTool = mcpTool;
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return serverName + ":" + mcpTool.getName();
    }

    @Override
    public String getDescription() {
        return mcpTool.getDescription();
    }

    @Override
    public Map<String, ToolParamDefinition> getParameters() {
        Map<String, ToolParamDefinition> params = new HashMap<>();
        
        if (mcpTool.getInputSchema() != null) {
            convertSchema(mcpTool.getInputSchema(), params);
        }
        
        return params;
    }

    private void convertSchema(Map<String, McpTypes.ParamSchema> schema, Map<String, ToolParamDefinition> params) {
        if (schema == null) return;
        
        for (Map.Entry<String, McpTypes.ParamSchema> entry : schema.entrySet()) {
            McpTypes.ParamSchema paramSchema = entry.getValue();
            ToolParamDefinition def = ToolParamDefinition.builder()
                    .name(entry.getKey())
                    .type(paramSchema.getType() != null ? paramSchema.getType() : "string")
                    .description(paramSchema.getDescription())
                    .required(paramSchema.isRequired())
                    .defaultValue(paramSchema.getDefaultValue())
                    .enums(paramSchema.getEnumValues())
                    .build();
            params.put(entry.getKey(), def);
        }
    }

    @Override
    public Object invoke(Map<String, Object> params) {
        try {
            Map<String, Object> arguments = convertArguments(params);
            
            log.info("Invoking MCP tool: {} on server: {}", mcpTool.getName(), serverName);
            
            return connection.callTool(mcpTool.getName(), arguments)
                    .map(result -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("contentType", result.getContentType());
                        response.put("content", result.getContent());
                        return response;
                    })
                    .onErrorResume(e -> {
                        log.error("MCP tool invocation failed: {}", mcpTool.getName(), e);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("error", e.getMessage());
                        return Mono.just(errorResponse);
                    })
                    .block();
        } catch (Exception e) {
            log.error("Failed to invoke MCP tool: {}", mcpTool.getName(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertArguments(Map<String, Object> params) {
        Map<String, Object> converted = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof JsonNode) {
                converted.put(entry.getKey(), objectMapper.convertValue(entry.getValue(), Object.class));
            } else {
                converted.put(entry.getKey(), entry.getValue());
            }
        }
        return converted;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    public String getServerName() {
        return serverName;
    }

    public McpTypes.Tool getMcpTool() {
        return mcpTool;
    }
}
