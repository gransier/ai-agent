package cn.gransier.config.listener;

import cn.gransier.domain.response.DifyChatResponse;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamListenerConfig {

    public StreamListenerConfig(StreamListenerRegistry registry) {
        // 注册实现策略
        registry.register(DifyChatResponse.class, (entity, listener) -> {
            if ("message_end".equals(entity.getEvent())) {
                listener.onComplete(entity);
                return;
            }
            String answer = entity.getAnswer() == null ? "" : entity.getAnswer();
            String escapedAnswer = answer.replace("\n", "<br/>")
                    .replace(" ", "&nbsp;");
            System.out.print(answer);
            entity.setAnswer(escapedAnswer);
            listener.onMessage(entity);
        });
    }
}
