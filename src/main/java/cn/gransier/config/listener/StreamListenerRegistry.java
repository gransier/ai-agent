package cn.gransier.config.listener;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StreamListenerRegistry {

    private final Map<Class<?>, StreamListenerTemplate<?>> registry = new HashMap<>();

    public <T> void register(Class<T> type, StreamListenerTemplate<T> template) {
        registry.put(type, template);
    }

    public <T> void dispatch(String line, StreamListener<T> listener) {
        @SuppressWarnings("unchecked")
        StreamListenerTemplate<T> template = (StreamListenerTemplate<T>) registry.get(listener.getType());
        if (template != null) {
            template.setListener(listener);
            template.process(line, listener);
        }
    }
}
