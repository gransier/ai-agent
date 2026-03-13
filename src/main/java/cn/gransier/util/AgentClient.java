package cn.gransier.util;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.config.AgentProperties;
import cn.gransier.config.listener.StreamListenerRegistry;
import cn.gransier.enums.AgentMethods;
import cn.gransier.config.listener.StreamListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AgentClient {

    private final OkHttpClient httpClient;

    private final String baseUrl;

    @Resource
    private StreamListenerRegistry registry;

    /**
     * 构造函数：允许自定义 OkHttp 客户端（用于超时、拦截器等）
     */
    public AgentClient(AgentProperties properties) {
        this.baseUrl = properties.getBaseUrl();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.SECONDS)
                .callTimeout(properties.getCallTimeout(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }


    /**
     * 通用 HTTP 调用方法（同步）
     *
     * @param annotation  注解配置（包含 endpoint、method）
     * @param apiKey      token
     * @param baseUrl     基础请求路径
     * @param requestBody 请求体
     * @return 响应结果
     */
    public <T> T http(AgentMethod annotation, String apiKey, String baseUrl, Object requestBody, Class<T> responseType) {
        Request request = getRequest(annotation, requestBody, apiKey, baseUrl);
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = response.body() != null ? response.body().string() : "Unknown error";
                throw new RuntimeException(errorMsg);
            }

            ResponseBody body = response.body();
            if (body == null || responseType == void.class) {
                return null;
            }

            String responseBody = body.string();
            if (responseType == String.class) {
                @SuppressWarnings("unchecked")
                T stringResponse = (T) responseBody;
                return stringResponse;
            }
            return JsonUtils.parseJson(responseBody, responseType);
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed", e);
        }
    }


    /**
     * 通用流式调用方法
     *
     * @param annotation  注解配置（包含 endpoint、method）
     * @param apiKey      token
     * @param baseUrl     基础请求路径
     * @param requestBody 请求体（会自动转为 JSON）
     * @param listener    流式回调监听器
     */
    public <T> void stream(
            AgentMethod annotation,
            String apiKey,
            String baseUrl,
            Object requestBody,
            StreamListener<T> listener) {
        Request request = getRequest(annotation, requestBody, apiKey, baseUrl);
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
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();
                            try {
                                T entity = JsonUtils.parseJson(data, listener.getType());
                                registry.dispatch(entity, listener);
                            } catch (Exception e) {
                                log.error("接收SSE异常:{}", e.getMessage());
                                listener.onError(new RuntimeException(data, e));
                            }
                        } else {
                            if (StringUtils.hasText(line) && !"event: ping".equals(line)) {
                                log.warn(line);
                                listener.onError(new RuntimeException(line));
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 构建请求
     *
     * @param agentMethod 请求配置注解
     * @param requestBody 请求体
     * @param apiKey      API key (passed from caller to avoid ThreadLocal issues with async)
     * @param baseUrl     基础请求路径
     * @return 请求包装
     */
    private Request getRequest(AgentMethod agentMethod, Object requestBody, String apiKey, String baseUrl) {
        AgentMethods method = agentMethod.method();
        String endpoint = agentMethod.endpoint();

        if (PathTemplateRenderer.containsPathVariables(endpoint)) {
            Map<String, Object> map = JsonUtils.parseMap(requestBody);
            endpoint = PathTemplateRenderer.render(endpoint, map);
        }
        String fullUrl = buildFullUrl(baseUrl, endpoint);
        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json");

        if (AgentMethods.GET == method) {
            if (requestBody != null) {
                String queryString = buildQueryString(requestBody);
                fullUrl = fullUrl + (fullUrl.contains("?") ? "&" : "?") + queryString;
                requestBuilder.url(fullUrl);
            }
            requestBuilder.get();
        } else {
            String jsonBody;
            try {
                jsonBody = JsonUtils.toJson(requestBody);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert request body to JSON", e);
            }

            RequestBody body = RequestBody.create(
                    MediaType.get("application/json"),
                    jsonBody);

            switch (method) {
                case POST -> requestBuilder.post(body);
                case PUT -> requestBuilder.put(body);
                case DELETE -> requestBuilder.delete(body);
                case PATCH -> requestBuilder.patch(body);
                default -> requestBuilder.method(method.name(), body);
            }
        }

        return requestBuilder.build();
    }

    private String buildFullUrl(String baseUrl, String endpoint) {
        String suffix = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl + suffix;
        }
        return this.baseUrl + suffix;
    }

    /**
     * 将对象转换为 URL 查询字符串
     *
     * @param obj 参数对象
     * @return 查询字符串，如 "key1=value1&key2=value2"
     */
    private String buildQueryString(Object obj) {
        if (obj == null) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!queryString.isEmpty()) {
                    queryString.append("&");
                }
                try {
                    String key = URLEncoder.encode(String.valueOf(entry.getKey()), StandardCharsets.UTF_8);
                    String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                    queryString.append(key).append("=").append(value);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to encode query parameter", e);
                }
            }
        } else {
            ObjectMapper mapper = JsonUtils.getObjectMapper();
            try {
                Map<String, Object> map = mapper.convertValue(obj, new TypeReference<>() {
                });
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (!queryString.isEmpty()) {
                        queryString.append("&");
                    }
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                    queryString.append(key).append("=").append(value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert object to query string", e);
            }
        }

        return queryString.toString();
    }

}