package cn.gransier.util;

import cn.gransier.common.DifyStreamListener;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Component
public class DifyClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = JsonUtils.getObjectMapper();
    @Getter
    private final String baseUrl = "http://192.168.2.207/";

    /**
     * 构造函数：允许自定义 OkHttp 客户端（用于超时、拦截器等）
     */
    public DifyClient() {
        this(new OkHttpClient.Builder()
                .callTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build());
    }

    public DifyClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 通用流式调用方法
     *
     * @param apiKey           Dify API Key
     * @param endpoint         接口路径，如 "v1/chat-messages"
     * @param requestBody      请求体（会自动转为 JSON）
     * @param contentExtractor 从 event 中提取内容的函数，默认提取 "answer" 字段
     * @param listener         流式回调监听器
     */
    public void stream(
            String apiKey,
            String endpoint,
            Object requestBody,
            Function<JsonNode, String> contentExtractor,
            DifyStreamListener listener) {

        String fullUrl = this.baseUrl + (endpoint.startsWith("/") ? endpoint.substring(1) : endpoint);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            listener.onError(new IllegalArgumentException("Failed to serialize request body", e));
            return;
        }

        Request request = getRequest(apiKey, fullUrl, jsonBody);

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body == null) {
                    listener.onComplete(null);
                    return;
                }
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {

                    String line;
                    String conversationId = null;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();

                            if ("[DONE]".equals(data)) {
                                listener.onComplete(conversationId);
                                return;
                            }

                            try {
                                JsonNode event = objectMapper.readTree(data);

                                // 提取内容（默认是 answer，但可自定义）
                                String content = contentExtractor.apply(event);
                                if (content != null && !content.isEmpty()) {
                                    listener.onMessage(content);
                                }

                                // 尝试提取 conversation_id（通用字段）
                                if (conversationId == null && event.has("conversation_id")) {
                                    conversationId = event.get("conversation_id").asText();
                                }
                                // 兼容 workflow 等场景：可能叫 task_id 或没有
                                // 可扩展：通过 listener 回调整个 event

                            } catch (Exception e) {
                                listener.onError(new RuntimeException("Parse SSE data error: " + data, e));
                            }
                        }
                    }
                }
            }
        });
    }

    private Request getRequest(String apiKey, String fullUrl, String jsonBody) {
        return new Request.Builder()
                .url(fullUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonBody))
                .build();
    }

    /**
     * 便捷方法：使用默认 contentExtractor（提取 "answer" 字段）
     */
    public void stream(String apiKey, String endpoint, Object requestBody, DifyStreamListener listener) {
        stream(apiKey, endpoint, requestBody, node -> {
            JsonNode answer = node.get("answer");
            return answer != null ? answer.asText() : "";
        }, listener);
    }

}