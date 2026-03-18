package cn.gransier.common.config.listener;

import reactor.core.publisher.FluxSink;

public class FluxStreamListener<T> implements StreamListener<T> {

    private final FluxSink<T> sink;
    private final Class<T> type;

    private FluxStreamListener(FluxSink<T> sink, Class<T> type) {
        this.sink = sink;
        this.type = type;
    }

    public static <T> StreamListener<T> newInstance(FluxSink<T> sink, Class<T> type) {
        return new FluxStreamListener<>(sink, type);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void onMessage(T message) {
        sink.next(message);
    }

    @Override
    public void onComplete(T complete) {
        sink.complete();
    }

    @Override
    public void onError(Throwable error) {
        sink.error(error);
    }
}
