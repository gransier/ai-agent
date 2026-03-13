package cn.gransier.listener;

import cn.gransier.domain.response.DifyChatResponse;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

public class FluxDifyStreamListener<T> implements DifyStreamListener<T> {

    private final FluxSink<T> sink;
    private final Class<T> type;

    private FluxDifyStreamListener(FluxSink<T> sink, Class<T> type) {
        this.sink = sink;
        this.type = type;
    }

    public static <T> DifyStreamListener<T> newInstance(FluxSink<T> sink, Class<T> type) {
        return new FluxDifyStreamListener<>(sink, type);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Consumer<T> consumer() {
        return (entity) -> {
            // todo 维护一个注册表(Map<Class,Consumer>)，用以注册不同的类
            if (entity instanceof DifyChatResponse difyChatResponse){
                if ("message_end".equals(difyChatResponse.getEvent())) {
                    onComplete(entity);
                    return;
                }
                String answer = difyChatResponse.getAnswer() == null ? "" : difyChatResponse.getAnswer();
                String escapedAnswer = answer.replace("\n", "<br/>")
                        .replace(" ", "&nbsp;");
                System.out.print(answer);
                difyChatResponse.setAnswer(escapedAnswer);
                onMessage(entity);
            }
        };
    }

    @Override
    public void onMessage(T message) {
        sink.next(message);
    }

    @Override
    public void onComplete(T complete) {
        onMessage(complete);
        sink.complete();
    }

    @Override
    public void onError(Throwable error) {
        sink.error(error);
    }
}
