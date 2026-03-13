package cn.gransier.config.listener;

import cn.gransier.domain.response.DifyChatResponse;
import cn.gransier.domain.response.GlmChatResponse;
import lombok.extern.slf4j.Slf4j;
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


        registry.register(GlmChatResponse.class, new StreamListenerTemplate<>() {
            @Override
            protected Optional<String> preHandle(String line) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) {
                        getListener().onComplete(new GlmChatResponse());
                        return Optional.empty();
                    }
                    return Optional.of(data);
                } else {
                    if (StringUtils.hasText(line) && !"event: ping".equals(line)) {
                        // 异常信息
                        return Optional.of(line);
                    }
                }
                return Optional.empty();
            }

            @Override
            protected boolean isEnd(GlmChatResponse entity) {
                if (entity.getError() != null) {
                    return true;
                }
                for (GlmChatResponse.Choice choice : entity.getChoices()) {
                    if ("stop".equals(choice.getFinishReason())) {
                        getListener().onComplete(entity);
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void handle(GlmChatResponse entity) {
                for (GlmChatResponse.Choice choice : entity.getChoices()) {
                    String reasoningContent = choice.getDelta().getReasoningContent();
                    String content = reasoningContent != null ? reasoningContent : choice.getDelta().getContent();
                    System.out.print(content == null ? "" : content);
                }
                getListener().onMessage(entity);
            }
        });
    }
}
