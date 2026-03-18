package cn.gransier;

import cn.gransier.common.util.JsonUtils;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpTest {
    public static void main(String[] args) {
        String host = args[0];
        String token = args[1];
        // 创建 OkHttpClient，设置足够长的超时（流式接口可能持续几十秒）
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();

        // 构造请求体
        Map<String, Object> params = new HashMap<>();
        params.put("inputs", new HashMap<>()); // 注意：必须是空对象 {}
        params.put("query", "介绍数智大屏项目的党建情况");
        params.put("response_mode", "streaming");
        params.put("conversation_id", "");
        params.put("user", "abc-123");

        // 序列化为 JSON（这里用简单字符串拼接，或替换为你的 JsonUtils）


        Request request = new Request.Builder()
                .url(host + "/chat-messages")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), JsonUtils.toJson(params)))
                .build();

        System.out.println("Sending request...");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Request failed: " + response.code() + " " + response.message());
                String errorBody = response.body() != null ? response.body().string() : "";
                System.err.println("Error body: " + errorBody);
                return;
            }

            System.out.println("Connected! Receiving stream...\n");

            // 关键：获取原始输入流，逐行读取 SSE 内容
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                System.out.println("Empty response body.");
                return;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8)
            );

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received: " + line);
                // 可选：如果收到 "[DONE]" 或特定结束标志，可 break
            }

            System.out.println("\nStream finished.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}