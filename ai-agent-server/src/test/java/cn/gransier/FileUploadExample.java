package cn.gransier;

import okhttp3.*;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

public class FileUploadExample {

    private static final String API_KEY = "app-0FNlqIqf0B4oQ0jFABhmzY9T";
    private static final String UPLOAD_URL = "http://192.168.2.207/v1/files/upload";
    private static final Tika tika = new Tika(); // 可复用，线程安全

    public static void main(String[] args) throws IOException {
        uploadFile("D:\\document\\docker\\sonarqube\\assert\\export.png", "abc-123");
    }

    public static void uploadFile(String filePath, String user) throws IOException {
        // 1. 创建 OkHttpClient（可复用）
        OkHttpClient client = new OkHttpClient();

        // 2. 准备文件
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        // 3. 推断 MIME 类型（简单版，可根据扩展名判断）
//        String mimeType = getMimeType(file.getName());
        String mimeType = tika.detect(file);

        // 4. 构建 multipart body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // 添加文件字段：name="file", 文件名, MIME 类型, 文件内容
                .addFormDataPart(
                        "file",
                        file.getName(),
                        RequestBody.create(MediaType.get(mimeType), file)
                )
                // 添加普通文本字段
                .addFormDataPart("user", user)
                .build();

        // 5. 构建请求
        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        // 6. 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Upload failed: " + response.code() + " " + response.message());
                assert response.body() != null;
                System.err.println("Response body: " + response.body().string());
                return;
            }
            assert response.body() != null;
            String responseBody = response.body().string();
            System.out.println("Upload success! Response: " + responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String detectMimeType(File file) throws IOException {
        String mimeType = tika.detect(file);
        return mimeType != null ? mimeType : "application/octet-stream";
    }
    // 简单 MIME 类型推断（生产环境建议用 Apache Tika 或 Files.probeContentType）
    private static String getMimeType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
