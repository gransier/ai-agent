package cn.gransier;

import cn.gransier.common.tool.domain.BaseTool;
import cn.gransier.common.tool.domain.FunctionResult;
import cn.gransier.common.tool.domain.ToolCall;
import cn.gransier.common.tool.executor.ToolExecutor;
import cn.gransier.common.tool.registry.ToolRegistry;
import cn.gransier.common.tool.resolver.CallChainManager;
import cn.gransier.common.tool.resolver.FunctionCallResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToolExecutorTest {

    private ToolRegistry registry;
    private ToolExecutor executor;
    private FunctionCallResolver resolver;
    private CallChainManager chainManager;

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry(null);
        executor = new ToolExecutor(registry);
        chainManager = new CallChainManager(10, 5);
        resolver = new FunctionCallResolver(executor, chainManager, new ObjectMapper());

        registry.register(new TestTool());
    }

    @Test
    void testToolExecution() {
        String toolCallId = "call_123";
        String toolName = "test_tool";
        Map<String, Object> params = Map.of("name", "World");

        FunctionResult result = executor.execute(toolCallId, toolName, params);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(toolCallId, result.getToolCallId());
        assertEquals(toolName, result.getToolName());
        assertEquals("Hello, World!", result.getResult());
    }

    @Test
    void testToolNotFound() {
        String toolCallId = "call_456";
        String toolName = "nonexistent_tool";
        Map<String, Object> params = Map.of();

        FunctionResult result = executor.execute(toolCallId, toolName, params);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Tool not found"));
    }

    @Test
    void testFunctionCallResolver() {
        List<ToolCall> toolCalls = List.of(
                ToolCall.builder()
                        .id("call_001")
                        .name("test_tool")
                        .arguments(Map.of("name", "Alice"))
                        .index(0)
                        .build()
        );

        List<FunctionResult> results = resolver.resolveAndExecute(toolCalls)
                .collectList()
                .block();

        assertNotNull(results);
        assertEquals(1, results.size());
        FunctionResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals("call_001", result.getToolCallId());
        assertEquals("Hello, Alice!", result.getResult());
    }

    @Test
    void testCallChainDepth() {
        CallChainManager manager = new CallChainManager(3, 5);

        assertFalse(manager.shouldTerminate());
        
        manager.enter("tool1", "call_1");
        assertFalse(manager.shouldTerminate());
        
        manager.enter("tool2", "call_2");
        assertFalse(manager.shouldTerminate());
        
        manager.enter("tool3", "call_3");
        assertTrue(manager.shouldTerminate());
        
        manager.reset();
        assertFalse(manager.shouldTerminate());
    }

    @Test
    void testToolRegistry() {
        assertFalse(registry.isEmpty());
        assertEquals(1, registry.size());
        assertTrue(registry.getToolNames().contains("test_tool"));

        List<Map<String, Object>> schemas = registry.toFunctionSchemas();
        assertEquals(1, schemas.size());
        assertEquals("test_tool", schemas.get(0).get("name"));
    }

    @Test
    void testTooManyToolsInOneTurn() {
        CallChainManager manager = new CallChainManager(10, 3);
        
        assertFalse(manager.shouldTerminateForToolCount(3));
        assertTrue(manager.shouldTerminateForToolCount(5));
    }

    static class TestTool implements BaseTool {
        @Override
        public String getName() {
            return "test_tool";
        }

        @Override
        public String getDescription() {
            return "测试工具";
        }

        @Override
        public Map<String, ToolParamDefinition> getParameters() {
            return Map.of(
                    "name", ToolParamDefinition.builder()
                            .name("name")
                            .type("string")
                            .description("名称")
                            .required(true)
                            .build()
            );
        }

        @Override
        public Object invoke(Map<String, Object> params) {
            String name = (String) params.get("name");
            return "Hello, " + name + "!";
        }
    }
}
