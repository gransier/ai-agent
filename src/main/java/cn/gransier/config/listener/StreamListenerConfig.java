package cn.gransier.config.listener;

import cn.gransier.domain.response.DifyChatResponse;
import cn.gransier.domain.response.GlmChatCompletionResponse;
import cn.gransier.util.JsonUtils;
import jakarta.validation.constraints.Null;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Configuration
public class StreamListenerConfig {

    public StreamListenerConfig(StreamListenerRegistry registry) {
        // 注册实现策略
        registry.register(DifyChatResponse.class, new StreamListenerTemplate<>() {
            @Override
            protected Optional<String> preHandle(String line) {
                if (line.startsWith("data: ")) {
                    return Optional.of(line.substring(6).trim());
                } else {
                    if (StringUtils.hasText(line) && !"event: ping".equals(line)) {
                        // 异常信息
                        return Optional.of(line);
                    }
                }
                return Optional.empty();
            }

            @Override
            protected boolean isEnd(DifyChatResponse entity) {
                // 异常编码
                if (entity.getStatus() >= 400) {
                    return true;
                }
                return "message_end".equals(entity.getEvent());
            }

            @Override
            protected void handle(DifyChatResponse entity) {
                if ("ping".equals(entity.getEvent())) {
                    return;
                }
                String answer = entity.getAnswer() == null ? "" : entity.getAnswer();
                String escapedAnswer = answer.replace("\n", "<br/>")
                        .replace(" ", "&nbsp;");
                System.out.print(answer);
                entity.setAnswer(escapedAnswer);
                getListener().onMessage(entity);
            }
        });


        registry.register(GlmChatCompletionResponse.class, new StreamListenerTemplate<>() {
            @Override
            protected boolean isEnd(GlmChatCompletionResponse entity) {
                if (entity.getError() != null) {
                    handle(entity);
                    return true;
                }
                for (GlmChatCompletionResponse.Choice choice : entity.getChoices()) {
                    if ("stop".equals(choice.getFinishReason())) {
                        getListener().onComplete(entity);
                        return true;
                    } else {
                        handle(entity);
                    }
                }
                return false;
            }
        });
    }
}
