package cn.gransier.common.mcp.client;

import cn.gransier.common.mcp.protocol.McpJsonRpc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class McpTransport {

    protected final ObjectMapper objectMapper;
    protected final Map<String, McpJsonRpc.Response> pendingRequests = new ConcurrentHashMap<>();
    protected final AtomicInteger idCounter = new AtomicInteger(1);
    protected final Sinks.Many<String> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    
    protected volatile boolean connected = false;

    protected McpTransport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public abstract void connect() throws IOException;
    public abstract void disconnect();
    public abstract boolean isConnected();

    public String sendRequest(String method, Object params) throws IOException {
        String id = String.valueOf(idCounter.getAndIncrement());
        McpJsonRpc.Request request = McpJsonRpc.Request.builder()
                .jsonrpc("2.0")
                .method(method)
                .params(params)
                .id(id)
                .build();
        
        String json = objectMapper.writeValueAsString(request);
        log.debug("Sending MCP request: {}", json);
        sendMessage(json);
        
        return id;
    }

    public void sendNotification(String method, Object params) throws IOException {
        McpJsonRpc.Request request = McpJsonRpc.Request.builder()
                .jsonrpc("2.0")
                .method(method)
                .params(params)
                .build();
        
        String json = objectMapper.writeValueAsString(request);
        log.debug("Sending MCP notification: {}", json);
        sendMessage(json);
    }

    protected abstract void sendMessage(String message) throws IOException;

    public Flux<String> receiveMessages() {
        return messageSink.asFlux();
    }

    protected void handleMessage(String message) {
        log.debug("Received MCP message: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            
            if (node.has("id")) {
                String id = node.get("id").asText();
                if (node.has("result")) {
                    pendingRequests.put(id, McpJsonRpc.Response.builder()
                            .jsonrpc("2.0")
                            .result(node.get("result"))
                            .id(id)
                            .build());
                } else if (node.has("error")) {
                    pendingRequests.put(id, McpJsonRpc.Response.builder()
                            .jsonrpc("2.0")
                            .error(McpJsonRpc.Error.builder()
                                    .code(node.get("error").get("code").asInt())
                                    .message(node.get("error").get("message").asText())
                                    .build())
                            .id(id)
                            .build());
                }
            }
            
            messageSink.tryEmitNext(message);
        } catch (Exception e) {
            log.error("Error handling MCP message: {}", message, e);
        }
    }

    public static class StdioTransport extends McpTransport {
        private Process process;
        private Thread readerThread;
        private OutputStreamWriter writer;
        private final String command;
        private final String[] args;

        public StdioTransport(ObjectMapper objectMapper) {
            super(objectMapper);
            this.command = null;
            this.args = null;
        }

        public StdioTransport(ObjectMapper objectMapper, String command, String... args) {
            super(objectMapper);
            this.command = command;
            this.args = args;
        }

        @Override
        public void connect() throws IOException {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            if (args != null) {
                for (String arg : args) {
                    builder.command().add(arg);
                }
            }
            builder.redirectErrorStream(true);
            
            process = builder.start();
            writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
            
            readerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        handleMessage(line);
                    }
                } catch (IOException e) {
                    log.error("Error reading from MCP process", e);
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();
            
            connected = true;
            log.info("Connected to MCP server via STDIO: {}", command);
        }

        @Override
        public void disconnect() {
            connected = false;
            if (process != null) {
                process.destroy();
            }
            if (readerThread != null) {
                readerThread.interrupt();
            }
            log.info("Disconnected from MCP server");
        }

        @Override
        public boolean isConnected() {
            return connected && process != null && process.isAlive();
        }

        @Override
        protected void sendMessage(String message) throws IOException {
            if (writer != null) {
                writer.write(message + "\n");
                writer.flush();
            }
        }
    }

    public static class HttpTransport extends McpTransport {
        private final String url;
        private final String apiKey;
        private volatile boolean closed = false;

        public HttpTransport(ObjectMapper objectMapper, String url, String apiKey) {
            super(objectMapper);
            this.url = url;
            this.apiKey = apiKey;
        }

        @Override
        public void connect() throws IOException {
            connected = true;
            log.info("Connected to MCP server via HTTP: {}", url);
        }

        @Override
        public void disconnect() {
            connected = false;
            closed = true;
            log.info("Disconnected from MCP server");
        }

        @Override
        public boolean isConnected() {
            return connected && !closed;
        }

        @Override
        protected void sendMessage(String message) throws IOException {
            log.debug("HTTP send (not implemented for demo): {}", message);
        }
    }
}
