package cn.gransier;

import cn.gransier.annotation.AgentServiceScan;
import cn.gransier.domain.query.AgentQuery;
import cn.gransier.service.AgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AgentServiceTest {

    @Resource
    private AgentService agentService;

    @Test
    void testAgentServiceIsInjected() {
        assertNotNull(agentService);
        System.out.println("AgentService bean: " + agentService);
        System.out.println("AgentService class: " + agentService.getClass());
        System.out.println("Is Proxy: " + java.lang.reflect.Proxy.isProxyClass(agentService.getClass()));
    }

    @Test
    void testCompletionsMethod() {
        AgentQuery query = new AgentQuery("你好", "user-123", "chat-001", true);
        Flux<String> result = agentService.completions(query);
        assertNotNull(result);
        System.out.println("completions方法返回: " + result.getClass());
    }
}
