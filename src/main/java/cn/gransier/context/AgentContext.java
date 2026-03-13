package cn.gransier.context;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal holder for API key to enable transparent access
 */
public class AgentContext {
    private static final ThreadLocal<Map<String, Object>> AGENT_HOLDER = ThreadLocal.withInitial(HashMap::new);

    private static final String API_KEY = "$API_KEY$";
    private static final String BASE_URL = "$BASE_URL$";
    public static void setApiKey(String apiKey) {
        AGENT_HOLDER.get().put(API_KEY, apiKey);
    }

    public static String getApiKey() {
        String apiKey = (String) AGENT_HOLDER.get().get(API_KEY);
        if (!StringUtils.hasText(apiKey)) {
            throw new RuntimeException("请求头缺失apiKey...");
        }
        return apiKey;
    }

    public static void setBaseUrl(String apiKey) {
        AGENT_HOLDER.get().put(BASE_URL, apiKey);
    }

    public static String getBaseUrl() {
        return (String) AGENT_HOLDER.get().get(BASE_URL);
    }

    public static void clear() {
        AGENT_HOLDER.remove();
    }
}