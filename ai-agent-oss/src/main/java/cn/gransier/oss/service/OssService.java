package cn.gransier.oss.service;

/**
 * 对象存储服务接口（简化版）
 * 用于统一操作 MinIO / 阿里云 OSS / AWS S3 等对象存储服务
 * 默认使用配置文件中指定的单一存储桶（bucket）
 */
public interface OssService {

    /**
     * 上传文件
     *
     * @param objectName  对象名称（即文件在 OSS 中的路径，如 "user/avatar/123.jpg"）
     * @param content     文件内容（字节数组）
     * @param contentType 文件 MIME 类型（如 "image/jpeg", "application/pdf"）
     * @return 文件的可访问 URL（如 http://localhost:9000/my-bucket/user/avatar/123.jpg）
     */
    String upload(String objectName, byte[] content, String contentType);

    /**
     * 下载文件
     *
     * @param objectName 对象名称（如 "docs/report.pdf"）
     * @return 文件内容字节数组
     * @throws RuntimeException 如果文件不存在或下载失败
     */
    byte[] download(String objectName);

    /**
     * 获取文件的访问链接
     *
     * @param objectName 对象名称
     * @return 可直接访问的 URL（永久链接，要求 bucket 或对象有公开读权限）
     * 若需临时链接（带签名），可另设计方法或通过参数控制
     */
    String getUrl(String objectName);

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    void delete(String objectName);

    /**
     * 获取带签名的临时访问链接（安全下载用）
     *
     * @param objectName 对象名
     * @param expires    过期时间（秒）
     * @return 临时可访问 URL
     */
    String getPresignedUrl(String objectName, int expires);
}
