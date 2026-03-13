package cn.gransier.config.listener;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class StreamListenerRegistry {

    private final Map<Class<?>, BiConsumer<?, ?>> registry = new HashMap<>();

    public <T> void register(Class<T> type, BiConsumer<T, StreamListener<T>> consumer) {
        registry.put(type, consumer);
    }

    @SuppressWarnings("unchecked")
    public <T> void dispatch(T entity, StreamListener<T> listener) {
        BiConsumer<?, ?> consumer = registry.get(listener.getType());
        if (consumer != null) {
            ((BiConsumer<T, StreamListener<T>>) consumer).accept(entity, listener);
        }
    }
}
