package cn.gransier.config.listener;

import cn.gransier.domain.response.DifyChatResponse;
import cn.gransier.domain.response.GlmChatCompletionResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(value = StreamListenerConfig.class, name = "streamListenerConfig")
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

        registry.register(GlmChatCompletionResponse.class, (entity, listener) -> {
            System.out.println(entity.toString());
            for (GlmChatCompletionResponse.Choice choice : entity.getChoices()) {
                if (!"stop".equals(  choice.getFinishReason())) {
                    System.out.print(choice.getMessage());
                }
            }
            for (GlmChatCompletionResponse.Choice choice : entity.getChoices()) {
                if ("stop".equals(  choice.getFinishReason())) {
                    listener.onComplete(entity);
                    return;
                }
            }
            listener.onMessage(entity);
        });
    }
}
