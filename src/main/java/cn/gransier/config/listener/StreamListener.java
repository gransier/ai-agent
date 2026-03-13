package cn.gransier.config.listener;

public interface StreamListener<T> {

    Class<T> getType();

    void onMessage(T message);

    void onComplete(T complete);

    void onError(Throwable error);
}
