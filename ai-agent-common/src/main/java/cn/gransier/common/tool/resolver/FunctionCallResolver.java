package cn.gransier.common.tool.resolver;

import cn.gransier.common.tool.domain.FunctionResult;
import cn.gransier.common.tool.domain.ToolCall;
import cn.gransier.common.tool.executor.ToolExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class FunctionCallResolver {

    private final ToolExecutor executor;
    private final CallChainManager chainManager;
    private final ObjectMapper objectMapper;

    public FunctionCallResolver(ToolExecutor executor, CallChainManager chainManager, ObjectMapper objectMapper) {
        this.executor = executor;
        this.chainManager = chainManager;
        this.objectMapper = objectMapper;
    }

    public Flux<FunctionResult> resolveAndExecute(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return Flux.empty();
        }

        if (chainManager.shouldTerminateForToolCount(toolCalls.size())) {
            log.warn("Too many tool calls in one turn: {}, max allowed: {}", 
                     toolCalls.size(), CallChainManager.DEFAULT_MAX_TOOLS_PER_TURN);
            return Flux.just(FunctionResult.error(null, "SYSTEM", 
                    "Too many tool calls: " + toolCalls.size()));
        }

        List<ToolCall> processedCalls = new ArrayList<>();
        for (int i = 0; i < toolCalls.size(); i++) {
            ToolCall call = toolCalls.get(i);
            if (call.getId() == null || call.getId().isEmpty()) {
                call.setId(generateToolCallId());
            }
            if (call.getIndex() == null) {
                call.setIndex(i);
            }
            processedCalls.add(call);
        }

        return Flux.fromIterable(processedCalls)
                .flatMap(this::executeToolCall);
    }

    public Mono<FunctionResult> executeToolCall(ToolCall toolCall) {
        chainManager.enter(toolCall.getName(), toolCall.getId());
        
        return executor.executeAsync(toolCall.getId(), toolCall.getName(), toolCall.getArguments())
                .doOnNext(result -> chainManager.exit(toolCall.getId()))
                .doOnError(e -> {
                    chainManager.exit(toolCall.getId());
                    log.error("Tool call failed: {}", toolCall.getName(), e);
                });
    }

    public Flux<String> resolveAndExecuteStream(List<ToolCall> toolCalls) {
        return resolveAndExecute(toolCalls)
                .map(this::formatResult);
    }

    public String formatResult(FunctionResult result) {
        if (result == null) {
            return "";
        }
        
        if (result.getError() != null) {
            return String.format("[%s] %s: %s", 
                    result.getToolCallId(), 
                    result.getToolName(), 
                    result.getError());
        }
        
        try {
            String jsonResult = objectMapper.writeValueAsString(result.getResult());
            return String.format("[%s] %s: %s", 
                    result.getToolCallId(), 
                    result.getToolName(), 
                    jsonResult);
        } catch (JsonProcessingException e) {
            return String.format("[%s] %s: %s", 
                    result.getToolCallId(), 
                    result.getToolName(), 
                    result.getResult());
        }
    }

    public String formatResultAsJson(FunctionResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public FunctionResult parseToolCallFromLLM(String llmResponse) {
        try {
            return objectMapper.readValue(llmResponse, FunctionResult.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse LLM response: {}", llmResponse, e);
            return null;
        }
    }

    public List<ToolCall> parseToolCallsFromLLMResponse(Map<String, Object> llmResponse) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        Object toolCallObj = llmResponse.get("tool_calls");
        if (toolCallObj instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Map<?, ?> toolCallMap) {
                    ToolCall toolCall = parseToolCallFromMap(toolCallMap, i);
                    if (toolCall != null) {
                        toolCalls.add(toolCall);
                    }
                }
            }
        }
        
        return toolCalls;
    }

    @SuppressWarnings("unchecked")
    private ToolCall parseToolCallFromMap(Map<?, ?> map, int index) {
        try {
            String id = (String) map.get("id");
            if (id == null) {
                id = generateToolCallId();
            }
            
            String name = (String) map.get("name");
            
            Object argsObj = map.get("arguments");
            Map<String, Object> arguments;
            if (argsObj instanceof String argsStr) {
                arguments = objectMapper.readValue(argsStr, Map.class);
            } else if (argsObj instanceof Map) {
                arguments = (Map<String, Object>) argsObj;
            } else {
                arguments = Map.of();
            }
            
            return ToolCall.builder()
                    .id(id)
                    .name(name)
                    .arguments(arguments)
                    .index(index)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse tool call from map: {}", map, e);
            return null;
        }
    }

    private String generateToolCallId() {
        return "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public void resetChain() {
        chainManager.reset();
    }

    public int getCurrentDepth() {
        return chainManager.getCurrentDepth();
    }

    public boolean shouldTerminate() {
        return chainManager.shouldTerminate();
    }
}
