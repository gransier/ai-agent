package cn.gransier.common;

import reactor.core.publisher.FluxSink;

public class DefaultDifyStreamListener implements DifyStreamListener {

    private final FluxSink<String> sink;

    private DefaultDifyStreamListener(FluxSink<String> sink) {
        this.sink = sink;
    }
    public static DifyStreamListener newInstance(FluxSink<String> sink) {
        return new DefaultDifyStreamListener(sink);
    }

    @Override
    public void onMessage(String answer) {
        sink.next(answer);
    }

    @Override
    public void onComplete(String conversationId) {
        sink.complete();
    }

    @Override
    public void onError(Throwable error) {
        sink.error(error);
    }
}
