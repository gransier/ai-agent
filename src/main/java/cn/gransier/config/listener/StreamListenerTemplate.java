package cn.gransier.config.listener;

import cn.gransier.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 流式请求解析模板
 */
@Slf4j
@Data
public abstract class StreamListenerTemplate<T> {

    private StreamListener<T> listener;

    public void process(String line, StreamListener<T> listener) {
        // 预处理
        Optional<String> opt = preHandle(line);
        if (opt.isEmpty()) {
            return;
        }
        String json = opt.get();
        try {
            // 解析json
            T entity = JsonUtils.parseJson(json, listener.getType());
            if (isEnd(entity)) {
                listener.onMessage(entity);
                listener.onComplete(entity);
                return;
            }
            handle(entity);
        } catch (Exception e) {
            log.error("接收SSE异常:{} json:{}", e.getMessage(), json);
            listener.onError(new RuntimeException(json, e));
        }
    }

    protected Optional<String> preHandle(String line) {
        return Optional.of(line);
    }

    abstract protected boolean isEnd(T entity);

    protected void handle(T entity) {
        getListener().onMessage(entity);
    }

}
