package cn.gransier.common.util;

import cn.gransier.common.annotation.AgentParam;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class UploadUtils {

    private static final Tika tika = new Tika();

    public static String detectMimeType(File file) {
        try {
            String mimeType = tika.detect(file);
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultipartBody getMultipartBody(Method method, Object[] args) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        String[] names = new String[1];
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Optional<RequestBody> opt = getRequestBody(arg, names);
            AgentParam paramAnnotation = parameters[i].getAnnotation(AgentParam.class);
            String paramName = paramAnnotation != null ? paramAnnotation.value() : parameters[i].getName();
            if (opt.isPresent()) {

                builder.addFormDataPart(paramName, names[0], opt.get());
            } else {
                builder.addFormDataPart(paramName, arg.toString());
            }
        }
        return builder.build();
    }

    private static Optional<RequestBody> getRequestBody(Object arg, String[] names) {
        if (arg instanceof File file) {
            String mimeType = detectMimeType(file);
            names[0] = file.getName();
            return Optional.of(RequestBody.create(MediaType.get(mimeType), file));
        }
        if (arg instanceof MultipartFile file) {
            String contentType = file.getContentType();
            names[0] = file.getOriginalFilename();
            return Optional.of(new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.get(contentType != null ? contentType : "application/octet-stream");
                }

                @Override
                public void writeTo(@NonNull BufferedSink sink) throws IOException {
                    try (InputStream in = file.getInputStream()) {
                        sink.writeAll(Okio.source(in));
                    }
                }
            });
        }
        return Optional.empty();
    }

//    @SneakyThrows
//    public static File multipartToFile(MultipartFile multipartFile) {
//        // 1. 创建临时文件（自动加随机后缀，避免冲突）
//        File tempFile = Files.createTempFile(
//                "upload_",
//                "." + getExtension(multipartFile.getOriginalFilename())
//        ).toFile();
//
//        // 2. 将 MultipartFile 内容写入临时文件
//        multipartFile.transferTo(tempFile);
//
//        // 3. 【重要】注册钩子，JVM 退出时自动删除（防止磁盘占满）
//        tempFile.deleteOnExit();
//
//        return tempFile;
//    }
//
//    // 辅助方法：获取文件扩展名
//    private static String getExtension(String filename) {
//        if (filename == null || filename.lastIndexOf('.') == -1) {
//            return "";
//        }
//        return filename.substring(filename.lastIndexOf('.') + 1);
//    }
}
