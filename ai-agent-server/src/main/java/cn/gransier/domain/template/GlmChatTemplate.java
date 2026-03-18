package cn.gransier.domain.template;

import cn.gransier.common.config.listener.StreamListenerTemplate;
import cn.gransier.domain.response.GlmChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class GlmChatTemplate extends StreamListenerTemplate<GlmChatResponse> {

    @Override
    public Class<GlmChatResponse> getType() {
        return GlmChatResponse.class;
    }

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
}
