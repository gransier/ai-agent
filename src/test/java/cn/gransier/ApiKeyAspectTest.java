package cn.gransier;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.context.ApiKeyContext;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.enums.AgentMethods;
import cn.gransier.listener.DifyStreamListener;
import cn.gransier.util.AgentClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

@Slf4j
public class ApiKeyAspectTest {

    @Test
    public void testApiKeyExtraction() {
        // Test that ApiKeyContext works correctly
        ApiKeyContext.setApiKey("test-key");
        String key = ApiKeyContext.getApiKey();
        assert key.equals("test-key") : "Expected test-key, got " + key;
        ApiKeyContext.clear();
        assert ApiKeyContext.getApiKey() == null : "Expected null after clear";
    }

    @Test
    public void testAgentClientWithContext() {
        AgentClient client = new AgentClient(null);
        
        // Create a mock AgentMethod annotation
        AgentMethod agentMethod = (AgentMethod) Proxy.newProxyInstance(
                ApiKeyAspectTest.class.getClassLoader(),
                new Class<?>[]{AgentMethod.class},
                (proxy, method1, args) -> switch (method1.getName()) {
                    case "endpoint" -> "/test";
                    case "method" -> AgentMethods.POST;
                    case "annotationType" -> AgentMethod.class;
                    default -> throw new UnsupportedOperationException("Unsupported method: " + method1.getName());
                }
        );

        // Test with context set
        ApiKeyContext.setApiKey("context-key");
        // We can't easily test the actual HTTP call without mocking, but we can verify
        // that the context is accessible
        String contextKey = ApiKeyContext.getApiKey();
        assert contextKey.equals("context-key") : "Expected context-key, got " + contextKey;
        
        ApiKeyContext.clear();
    }
}