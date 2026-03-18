package cn.gransier.domain.template;

import cn.gransier.common.config.listener.StreamListenerTemplate;
import cn.gransier.domain.response.DifyChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class DifyChatTemplate extends StreamListenerTemplate<DifyChatResponse> {

    @Override
    public Class<DifyChatResponse> getType() {
        return DifyChatResponse.class;
    }

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
}
