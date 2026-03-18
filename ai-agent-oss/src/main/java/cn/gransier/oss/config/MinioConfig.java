package cn.gransier.oss.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "oss.type", havingValue = "minio")
public class MinioConfig {

    @Resource
    private OssProperties ossProperties;

    @Bean
    @SneakyThrows
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(
                        ossProperties.getEndpoint()).credentials(ossProperties.getAccessKey(),
                        ossProperties.getSecretKey()
                ).build();
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(ossProperties.getBucketName()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(ossProperties.getBucketName()).build());
        }
        return minioClient;
    }
}
