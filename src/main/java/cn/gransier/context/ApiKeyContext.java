package cn.gransier.context;

import org.springframework.util.StringUtils;

/**
 * ThreadLocal holder for API key to enable transparent access
 */
public class ApiKeyContext {
    private static final ThreadLocal<String> API_KEY_HOLDER = new ThreadLocal<>();

    public static void setApiKey(String apiKey) {
        API_KEY_HOLDER.set(apiKey);
    }

    public static String getApiKey() {
        String apiKey = API_KEY_HOLDER.get();
        if (!StringUtils.hasText(apiKey)) {
            throw new RuntimeException("请求头缺失apiKey...");
        }
        return apiKey;
    }

    public static void clear() {
        API_KEY_HOLDER.remove();
    }
}