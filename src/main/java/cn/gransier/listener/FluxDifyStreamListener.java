package cn.gransier.listener;

import reactor.core.publisher.FluxSink;

public class FluxDifyStreamListener implements DifyStreamListener {

    private final FluxSink<String> sink;

    private FluxDifyStreamListener(FluxSink<String> sink) {
        this.sink = sink;
    }
    public static DifyStreamListener newInstance(FluxSink<String> sink) {
        return new FluxDifyStreamListener(sink);
    }

    @Override
    public void onMessage(String data) {
        sink.next(data);
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
