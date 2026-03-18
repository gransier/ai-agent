package cn.gransier.common.config.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StreamListenerRegistry {

    private final Map<Class<?>, StreamListenerTemplate<?>> registry = new HashMap<>();

    public <T> void register(StreamListenerTemplate<T> template) {
        registry.put(template.getType(), template);
    }

    public <T> void dispatch(String line, StreamListener<T> listener) {
        @SuppressWarnings("unchecked")
        StreamListenerTemplate<T> template = (StreamListenerTemplate<T>) registry.get(listener.getType());
        if (template != null) {
            template.setListener(listener);
            template.process(line, listener);
        } else {
            log.warn("No template found for type: {}", listener.getType().getName());
            listener.onError(new IllegalArgumentException("No template found for type: " + listener.getType().getName()));
        }
    }
}
