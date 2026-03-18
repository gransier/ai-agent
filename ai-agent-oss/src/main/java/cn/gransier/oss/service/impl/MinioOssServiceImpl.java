package cn.gransier.oss.service.impl;

import cn.gransier.oss.config.OssProperties;
import cn.gransier.oss.service.OssService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

@Slf4j
@ConditionalOnProperty(name = "oss.type", havingValue = "minio")
@Service
public class MinioOssServiceImpl implements OssService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private OssProperties ossProperties;

    /**
     * 上传文件
     */
    @Override
    public String upload(String objectName, byte[] content, String contentType) {
        try {
            // 确保存储桶存在
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(ossProperties.getBucketName()).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(ossProperties.getBucketName()).build());
            }

            // 上传对象
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectName)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                    .build());

            log.info("文件上传成功: {}", objectName);
            return getUrl(objectName);

        } catch (Exception e) {
            log.error("MinIO 文件上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 下载文件
     */
    @Override
    public byte[] download(String objectName) {
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(ossProperties.getBucketName())
                            .object(objectName)
                            .build()
            );

            return response.readAllBytes();

        } catch (Exception e) {
            log.error("MinIO 文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 获取文件的公开访问 URL
     * 注意：此 URL 可访问的前提是：
     * 1. MinIO 已配置为允许匿名读（或 bucket 设置为 public）
     * 2. 或者你使用的是内网地址（如 localhost），且服务可直连
     * 若需带签名的临时链接，请使用 presignedGetObject
     */
    @Override
    public String getUrl(String objectName) {
        // 构造公开 URL：http://endpoint/bucket/objectName
        String endpoint = ossProperties.getEndpoint();
        String bucket = ossProperties.getBucketName();

        // 处理 endpoint 末尾斜杠
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }

        // URL 编码 objectName（简单处理，生产建议用 URLEncoder）
        String encodedObjectName = objectName.replace(" ", "%20");

        return endpoint + bucket + "/" + encodedObjectName;
    }

    /**
     * 删除文件
     */
    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("MinIO 文件删除失败: {}", objectName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expires) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(ossProperties.getBucketName())
                            .object(objectName)
                            .expiry(expires)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名 URL 失败: {}", objectName, e);
            throw new RuntimeException("生成临时链接失败", e);
        }
    }
}