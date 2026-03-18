package cn.gransier.oss.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static String getObjectName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // 生成唯一文件名（可选：加上时间戳、UUID 等）
        return System.currentTimeMillis() + "-" + originalFilename;
    }

    public static String getContentType(MultipartFile file) {
        // 获取 MIME 类型
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    public static void doDownload(String fileName, HttpServletResponse response, byte[] content) throws IOException {
        String originalFileName = fileName.substring(fileName.lastIndexOf("/") + 1);

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=" +
                        URLEncoder.encode(originalFileName, StandardCharsets.UTF_8));
        response.setContentLength(content.length);

        // 写入响应流
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }
}
