package cn.gransier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cn.gransier.common.annotation.AgentServiceScan;

/**
 * Hello world!
 */
@AgentServiceScan("cn.gransier.service")
@SpringBootApplication
public class AiAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }
}
