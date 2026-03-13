package cn.gransier.listener;

import java.util.function.Consumer;

public interface DifyStreamListener<T> {

    Class<T> getType();

    Consumer<T> consumer();

    void onMessage(T message);

    void onComplete(T complete);

    void onError(Throwable error);
}
