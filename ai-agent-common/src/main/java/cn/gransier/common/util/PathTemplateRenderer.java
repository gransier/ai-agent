package cn.gransier.common.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathTemplateRenderer {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    /**
     * 将模板路径中的 {key} 替换为 params 中对应的值
     *
     * @param template 路径模板，如 "/conversations/{conversation_id}/{id}"
     * @param params   参数映射，如 Map.of("conversation_id", "666", "id", "777")
     * @return 替换后的路径，如 "/conversations/666/777"
     */
    public static String render(String template, Map<String, Object> params) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1); // 提取花括号内的名字，如 "conversation_id"
            String value = String.valueOf(params.get(key));
            if (value == null) {
                throw new IllegalArgumentException("Missing path parameter: " + key);
            }
            // 转义特殊字符（虽然路径中一般不需要，但安全起见）
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 判断给定的路径是否包含路径变量占位符，例如 {id}, {user_name}
     *
     * @param path 路径字符串
     * @return 如果包含 {xxx} 形式的占位符，返回 true；否则 false
     */
    public static boolean containsPathVariables(String path) {
        if (path == null) {
            return false;
        }
        return PATH_VARIABLE_PATTERN.matcher(path).find();
    }

}