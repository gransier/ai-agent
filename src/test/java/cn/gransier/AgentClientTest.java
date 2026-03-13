package cn.gransier;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.enums.AgentMethods;
import cn.gransier.listener.DifyStreamListener;
import cn.gransier.util.AgentClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

@Slf4j
public class AgentClientTest {
    @Test
    public void testStream() {
        AgentClient difyClient = new AgentClient(null);

        // 方式 1：使用动态代理创建注解实例
        AgentMethod agentMethod = createAgentMethod(
                "/chat-messages",
                AgentMethods.POST
        );

        // 构建请求参数
        DifyChatQuery difyChatQuery = new DifyChatQuery("hello", "user-123", "");


        // 测试流式调用
        difyClient.stream(agentMethod, difyChatQuery, new DifyStreamListener() {
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
    }

    @Test
    public void testHttp() {
        AgentClient difyClient = new AgentClient(null);
        AgentMethod agentMethod = createAgentMethod(
                "/conversations",
                AgentMethods.GET
        );
        Map<Object, Object> map = Map.of(
                "user", "user-123"
        );
        String object = difyClient.http(agentMethod, map, String.class);
        System.out.println(object);
    }

    /**
     * 使用动态代理创建 AgentMethod 注解的实例
     */
    private static AgentMethod createAgentMethod(String endpoint, AgentMethods method) {
        return (AgentMethod) Proxy.newProxyInstance(
                AgentClientTest.class.getClassLoader(),
                new Class<?>[]{AgentMethod.class},
                (proxy, method1, args) -> switch (method1.getName()) {
                    case "apiKey" -> "app-kn4j9PtM1SZLC5K7jL148MUm";
                    case "endpoint" -> endpoint;
                    case "method" -> method;
                    case "annotationType" -> AgentMethod.class;
                    default -> throw new UnsupportedOperationException("Unsupported method: " + method1.getName());
                }
        );
    }
}


