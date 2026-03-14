package cn.gransier;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.context.AgentContext;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.enums.AgentMethods;
import cn.gransier.config.listener.StreamListener;
import cn.gransier.util.AgentClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

@Slf4j
public class AgentClientTest {
    private final String apiKey = "ApiKeyContext.getApiKey()";

    @Test
    public void testStream() {
        AgentClient difyClient = new AgentClient(null, null, null);

        // 方式 1：使用动态代理创建注解实例
        AgentMethod agentMethod = createAgentMethod(
                "/chat-messages",
                AgentMethods.POST
        );

        // 构建请求参数
        DifyChatQuery difyChatQuery = new DifyChatQuery("hello", "user-123", "");

        // Set API key in ThreadLocal context (simulating interceptor behavior)
        AgentContext.setApiKey("test-api-key-from-header");

        // 测试流式调用
        difyClient.stream(agentMethod, apiKey, "", difyChatQuery, new StreamListener<String>() {
            @Override
            public Class<String> getType() {
                return null;
            }

            @Override
            public void onMessage(String message) {
                System.out.print(message);
            }

            @Override
            public void onComplete(String conversationId) {
                System.out.println("对话完成，ID: " + conversationId);
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("发生错误：" + error.getMessage());
            }
        });

        // Clean up ThreadLocal
        AgentContext.clear();
    }

    @Test
    public void testHttp() {
        AgentClient difyClient = new AgentClient(null, null, null);
        AgentMethod agentMethod = createAgentMethod(
                "/conversations",
                AgentMethods.GET
        );
        Map<Object, Object> map = Map.of(
                "user", "user-123"
        );

        // Set API key in ThreadLocal context (simulating interceptor behavior)
        AgentContext.setApiKey("test-api-key-from-header");

        String object = difyClient.http(agentMethod, apiKey, "", map, String.class);
        System.out.println(object);

        // Clean up ThreadLocal
        AgentContext.clear();
    }

    /**
     * 使用动态代理创建 AgentMethod 注解的实例
     */
    private static AgentMethod createAgentMethod(String endpoint, AgentMethods method) {
        return (AgentMethod) Proxy.newProxyInstance(
                AgentClientTest.class.getClassLoader(),
                new Class<?>[]{AgentMethod.class},
                (proxy, method1, args) -> switch (method1.getName()) {
                    case "endpoint" -> endpoint;
                    case "method" -> method;
                    case "annotationType" -> AgentMethod.class;
                    default -> throw new UnsupportedOperationException("Unsupported method: " + method1.getName());
                }
        );
    }
}


