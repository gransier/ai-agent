package cn.gransier.common.context;

import cn.gransier.common.tool.domain.FunctionResult;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AgentContext {
    private static final ThreadLocal<Map<String, Object>> AGENT_HOLDER = ThreadLocal.withInitial(HashMap::new);

    private static final String API_KEY = "$API_KEY$";
    private static final String BASE_URL = "$BASE_URL$";
    private static final String TOOLS = "$TOOLS$";
    private static final String FUNCTION_RESULTS = "$FUNCTION_RESULTS$";
    private static final String USER_ID = "$USER_ID$";
    private static final String SESSION_ID = "$SESSION_ID$";
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

    public static void setTools(List<Map<String, Object>> tools) {
        AGENT_HOLDER.get().put(TOOLS, tools);
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTools() {
        return (List<Map<String, Object>>) AGENT_HOLDER.get().get(TOOLS);
    }

    public static void addFunctionResult(FunctionResult result) {
        List<FunctionResult> results = getFunctionResults();
        if (results == null) {
            results = new LinkedList<>();
        }
        results.add(result);
        AGENT_HOLDER.get().put(FUNCTION_RESULTS, results);
    }

    @SuppressWarnings("unchecked")
    public static List<FunctionResult> getFunctionResults() {
        return (List<FunctionResult>) AGENT_HOLDER.get().get(FUNCTION_RESULTS);
    }

    public static void clearFunctionResults() {
        AGENT_HOLDER.get().put(FUNCTION_RESULTS, null);
    }

    public static void setUserId(String userId) {
        AGENT_HOLDER.get().put(USER_ID, userId);
    }

    public static String getUserId() {
        return (String) AGENT_HOLDER.get().get(USER_ID);
    }

    public static void setSessionId(String sessionId) {
        AGENT_HOLDER.get().put(SESSION_ID, sessionId);
    }

    public static String getSessionId() {
        return (String) AGENT_HOLDER.get().get(SESSION_ID);
    }

    public static void clear() {
        AGENT_HOLDER.remove();
    }
}