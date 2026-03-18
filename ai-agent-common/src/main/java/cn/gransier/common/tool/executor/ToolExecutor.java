package cn.gransier.common.tool.executor;

import cn.gransier.common.tool.domain.BaseTool;
import cn.gransier.common.tool.domain.FunctionResult;
import cn.gransier.common.tool.registry.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ToolExecutor {

    private final ToolRegistry registry;

    public ToolExecutor(ToolRegistry registry) {
        this.registry = registry;
    }

    public FunctionResult execute(String toolCallId, String toolName, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        
        Optional<BaseTool> toolOpt = registry.get(toolName);
        if (toolOpt.isEmpty()) {
            log.error("Tool not found: {}", toolName);
            return FunctionResult.error(toolCallId, toolName, "Tool not found: " + toolName);
        }

        BaseTool tool = toolOpt.get();
        
        try {
            validateParams(tool, params);
            Object result = tool.invoke(params);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Tool '{}' executed successfully in {}ms", toolName, duration);
            return FunctionResult.success(toolCallId, toolName, result, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Tool '{}' execution failed", toolName, e);
            FunctionResult errorResult = FunctionResult.error(toolCallId, toolName, e.getMessage());
            errorResult.setDuration(duration);
            return errorResult;
        }
    }

    public Mono<FunctionResult> executeAsync(String toolCallId, String toolName, Map<String, Object> params) {
        return Mono.fromCallable(() -> execute(toolCallId, toolName, params))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Object executeDirect(String toolName, Map<String, Object> params) {
        Optional<BaseTool> toolOpt = registry.get(toolName);
        if (toolOpt.isEmpty()) {
            throw new ToolNotFoundException("Tool not found: " + toolName);
        }
        
        try {
            validateParams(toolOpt.get(), params);
            return toolOpt.get().invoke(params);
        } catch (Exception e) {
            throw new ToolExecutionException("Tool execution failed: " + toolName, e);
        }
    }

    private void validateParams(BaseTool tool, Map<String, Object> params) {
        Map<String, BaseTool.ToolParamDefinition> definitions = tool.getParameters();
        for (Map.Entry<String, BaseTool.ToolParamDefinition> entry : definitions.entrySet()) {
            String paramName = entry.getKey();
            BaseTool.ToolParamDefinition definition = entry.getValue();
            
            if (definition.isRequired() && (!params.containsKey(paramName) || params.get(paramName) == null)) {
                throw new InvalidParameterException(
                    String.format("Required parameter '%s' is missing for tool '%s'", paramName, tool.getName())
                );
            }
        }
    }

    public static class ToolNotFoundException extends RuntimeException {
        public ToolNotFoundException(String message) {
            super(message);
        }
    }

    public static class ToolExecutionException extends RuntimeException {
        public ToolExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidParameterException extends RuntimeException {
        public InvalidParameterException(String message) {
            super(message);
        }
    }
}
