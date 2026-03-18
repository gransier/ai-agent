package cn.gransier.common.mcp.client;

import cn.gransier.common.mcp.protocol.McpJsonRpc;
import cn.gransier.common.mcp.protocol.McpTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class McpClient {

    private final ObjectMapper objectMapper;
    private final Map<String, McpServerConnection> connections = new ConcurrentHashMap<>();

    public McpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public McpServerConnection connect(String serverName, String command, String... args) throws IOException {
        McpTransport transport = new McpTransport.StdioTransport(objectMapper, command, args);
        return connect(serverName, transport);
    }

    public McpServerConnection connect(String serverName, McpTransport transport) throws IOException {
        transport.connect();
        
        McpServerConnection connection = new McpServerConnection(serverName, transport, objectMapper);
        connection.initialize();
        
        connections.put(serverName, connection);
        log.info("Connected to MCP server: {}", serverName);
        
        return connection;
    }

    public void disconnect(String serverName) {
        McpServerConnection connection = connections.remove(serverName);
        if (connection != null) {
            connection.close();
        }
    }

    public Optional<McpServerConnection> getConnection(String serverName) {
        return Optional.ofNullable(connections.get(serverName));
    }

    public Collection<McpServerConnection> getAllConnections() {
        return connections.values();
    }

    public Map<String, List<McpTypes.Tool>> getAllTools() {
        Map<String, List<McpTypes.Tool>> allTools = new HashMap<>();
        for (Map.Entry<String, McpServerConnection> entry : connections.entrySet()) {
            try {
                List<McpTypes.Tool> tools = entry.getValue().listTools();
                if (tools != null && !tools.isEmpty()) {
                    allTools.put(entry.getKey(), tools);
                }
            } catch (Exception e) {
                log.warn("Failed to get tools from server {}: {}", entry.getKey(), e.getMessage());
            }
        }
        return allTools;
    }

    @Slf4j
    public static class McpServerConnection {
        private final String name;
        private final McpTransport transport;
        private final ObjectMapper objectMapper;
        private final Map<String, Object> serverCapabilities = new HashMap<>();
        private String protocolVersion = "2024-11-05";
        private boolean initialized = false;

        public McpServerConnection(String name, McpTransport transport, ObjectMapper objectMapper) {
            this.name = name;
            this.transport = transport;
            this.objectMapper = objectMapper;
        }

        public void initialize() throws IOException {
            Map<String, Object> params = new HashMap<>();
            params.put("protocolVersion", protocolVersion);
            params.put("capabilities", Map.of(
                    "roots", Map.of("listChanged", true),
                    "sampling", Map.of()
            ));
            params.put("clientInfo", Map.of(
                    "name", "ai-agent",
                    "version", "1.0.0"
            ));

            String id = transport.sendRequest(McpJsonRpc.Methods.INITIALIZE, params);
            McpJsonRpc.Response response = waitForResponse(id, 30, TimeUnit.SECONDS);
            
            if (response.getError() != null) {
                throw new IOException("Initialize failed: " + response.getError().getMessage());
            }

            try {
                JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(response.getResult()));
                this.protocolVersion = result.has("protocolVersion") 
                        ? result.get("protocolVersion").asText() 
                        : this.protocolVersion;
                
                if (result.has("capabilities")) {
                    JsonNode caps = result.get("capabilities");
                    if (caps.has("tools")) serverCapabilities.put("tools", caps.get("tools"));
                    if (caps.has("resources")) serverCapabilities.put("resources", caps.get("resources"));
                    if (caps.has("prompts")) serverCapabilities.put("prompts", caps.get("prompts"));
                }
            } catch (Exception e) {
                log.warn("Failed to parse initialize response", e);
            }

            transport.sendNotification(McpJsonRpc.Methods.INITIALIZED, null);
            this.initialized = true;
            log.info("MCP server '{}' initialized with protocol version {}", name, protocolVersion);
        }

        public List<McpTypes.Tool> listTools() throws IOException {
            if (!isInitialized()) {
                throw new IllegalStateException("Server not initialized");
            }

            String id = transport.sendRequest(McpJsonRpc.Methods.TOOLS_LIST, null);
            McpJsonRpc.Response response = waitForResponse(id, 30, TimeUnit.SECONDS);
            
            if (response.getError() != null) {
                throw new IOException("List tools failed: " + response.getError().getMessage());
            }

            try {
                JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(response.getResult()));
                if (result.has("tools") && result.get("tools").isArray()) {
                    List<McpTypes.Tool> tools = new ArrayList<>();
                    for (JsonNode toolNode : result.get("tools")) {
                        tools.add(objectMapper.treeToValue(toolNode, McpTypes.Tool.class));
                    }
                    return tools;
                }
            } catch (Exception e) {
                log.error("Failed to parse tools list", e);
            }
            return Collections.emptyList();
        }

        public Mono<McpTypes.ToolCallResult> callTool(String toolName, Map<String, Object> arguments) {
            return callTool(toolName, arguments, 60, TimeUnit.SECONDS);
        }

        public Mono<McpTypes.ToolCallResult> callTool(String toolName, Map<String, Object> arguments, 
                                                      long timeout, TimeUnit unit) {
            return Mono.<McpTypes.ToolCallResult>create(emitter -> {
                try {
                    Map<String, Object> params = new HashMap<>();
                    params.put("name", toolName);
                    params.put("arguments", arguments);

                    String id = transport.sendRequest(McpJsonRpc.Methods.TOOLS_CALL, params);
                    
                    transport.receiveMessages()
                            .filter(msg -> {
                                try {
                                    JsonNode node = objectMapper.readTree(msg);
                                    return node.has("id") && id.equals(node.get("id").asText());
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .next()
                            .timeout(Duration.ofMillis(unit.toMillis(timeout)), Schedulers.boundedElastic())
                            .subscribe(
                                    msg -> {
                                        try {
                                            JsonNode node = objectMapper.readTree(msg);
                                            if (node.has("error")) {
                                                emitter.error(new IOException(
                                                        node.get("error").get("message").asText()));
                                            } else if (node.has("result")) {
                                                JsonNode resultNode = node.get("result");
                                                if (resultNode.has("content") && resultNode.get("content").isArray()) {
                                                    JsonNode contentNode = resultNode.get("content").get(0);
                                                    McpTypes.ToolCallResult result = McpTypes.ToolCallResult.builder()
                                                            .contentType(contentNode.has("type") ? contentNode.get("type").asText() : "text")
                                                            .content(contentNode.has("text") ? contentNode.get("text").asText() : "")
                                                            .build();
                                                    emitter.success(result);
                                                } else {
                                                    emitter.success(McpTypes.ToolCallResult.builder()
                                                            .contentType("text")
                                                            .content(resultNode.toString())
                                                            .build());
                                                }
                                            }
                                        } catch (Exception e) {
                                            emitter.error(e);
                                        }
                                    },
                                    emitter::error,
                                    () -> emitter.error(new TimeoutException("Tool call timeout"))
                            );
                } catch (Exception e) {
                    emitter.error(e);
                }
            }).subscribeOn(Schedulers.boundedElastic());
        }

        public List<McpTypes.Resource> listResources() throws IOException {
            if (!isInitialized()) {
                throw new IllegalStateException("Server not initialized");
            }

            String id = transport.sendRequest(McpJsonRpc.Methods.RESOURCES_LIST, null);
            McpJsonRpc.Response response = waitForResponse(id, 30, TimeUnit.SECONDS);
            
            if (response.getError() != null) {
                throw new IOException("List resources failed: " + response.getError().getMessage());
            }

            try {
                JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(response.getResult()));
                if (result.has("resources") && result.get("resources").isArray()) {
                    List<McpTypes.Resource> resources = new ArrayList<>();
                    for (JsonNode resourceNode : result.get("resources")) {
                        resources.add(objectMapper.treeToValue(resourceNode, McpTypes.Resource.class));
                    }
                    return resources;
                }
            } catch (Exception e) {
                log.error("Failed to parse resources list", e);
            }
            return Collections.emptyList();
        }

        public String readResource(String uri) throws IOException {
            if (!isInitialized()) {
                throw new IllegalStateException("Server not initialized");
            }

            Map<String, Object> params = Map.of("uri", uri);
            String id = transport.sendRequest(McpJsonRpc.Methods.RESOURCES_READ, params);
            McpJsonRpc.Response response = waitForResponse(id, 30, TimeUnit.SECONDS);
            
            if (response.getError() != null) {
                throw new IOException("Read resource failed: " + response.getError().getMessage());
            }

            try {
                JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(response.getResult()));
                if (result.has("contents") && result.get("contents").isArray()) {
                    JsonNode contentNode = result.get("contents").get(0);
                    if (contentNode.has("content")) {
                        return contentNode.get("content").asText();
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse resource content", e);
            }
            return null;
        }

        public List<McpTypes.Prompt> listPrompts() throws IOException {
            if (!isInitialized()) {
                throw new IllegalStateException("Server not initialized");
            }

            String id = transport.sendRequest(McpJsonRpc.Methods.PROMPTS_LIST, null);
            McpJsonRpc.Response response = waitForResponse(id, 30, TimeUnit.SECONDS);
            
            if (response.getError() != null) {
                throw new IOException("List prompts failed: " + response.getError().getMessage());
            }

            try {
                JsonNode result = objectMapper.readTree(objectMapper.writeValueAsString(response.getResult()));
                if (result.has("prompts") && result.get("prompts").isArray()) {
                    List<McpTypes.Prompt> prompts = new ArrayList<>();
                    for (JsonNode promptNode : result.get("prompts")) {
                        prompts.add(objectMapper.treeToValue(promptNode, McpTypes.Prompt.class));
                    }
                    return prompts;
                }
            } catch (Exception e) {
                log.error("Failed to parse prompts list", e);
            }
            return Collections.emptyList();
        }

        public Flux<String> receiveNotifications() {
            return transport.receiveMessages()
                    .filter(msg -> {
                        try {
                            JsonNode node = objectMapper.readTree(msg);
                            return !node.has("id");
                        } catch (Exception e) {
                            return false;
                        }
                    });
        }

        private McpJsonRpc.Response waitForResponse(String id, long timeout, TimeUnit unit) {
            long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
            
            while (System.currentTimeMillis() < deadline) {
                if (transport.pendingRequests.containsKey(id)) {
                    return transport.pendingRequests.remove(id);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            return McpJsonRpc.Response.builder()
                    .jsonrpc("2.0")
                    .error(McpJsonRpc.Error.builder()
                            .code(McpJsonRpc.ErrorCodes.INTERNAL_ERROR)
                            .message("Request timeout")
                            .build())
                    .id(id)
                    .build();
        }

        public boolean isInitialized() {
            return initialized;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getCapabilities() {
            return serverCapabilities;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public boolean isConnected() {
            return transport.isConnected();
        }

        public void close() {
            transport.disconnect();
            initialized = false;
            log.info("Closed MCP connection to server: {}", name);
        }
    }
}
