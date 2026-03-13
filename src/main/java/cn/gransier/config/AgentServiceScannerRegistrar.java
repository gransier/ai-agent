package cn.gransier.config;

import cn.gransier.annotation.AgentService;
import cn.gransier.annotation.AgentServiceScan;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AgentServiceScannerRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String RESOURCE_PATTERN = "/**/*.class";

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                        @NonNull BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(AgentServiceScan.class.getName());
        if (attributes == null) {
            return;
        }

        String[] basePackages = (String[]) attributes.get("value");
        if (basePackages == null || basePackages.length == 0) {
            // 默认扫描注解所在类的包
            basePackages = new String[]{ClassUtils.getPackageName(importingClassMetadata.getClassName())};
        }

        List<Class<?>> agentInterfaces = scanForAgentInterfaces(basePackages);
        for (Class<?> clazz : agentInterfaces) {
            registerAgentServiceBean(clazz, registry);
        }
    }

    private List<Class<?>> scanForAgentInterfaces(String[] basePackages) {
        List<Class<?>> result = new ArrayList<>();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

        for (String basePackage : basePackages) {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(basePackage) + RESOURCE_PATTERN;

            try {
                org.springframework.core.io.Resource[] resources = resourceResolver.getResources(packageSearchPath);
                for (org.springframework.core.io.Resource resource : resources) {
                    if (!resource.isReadable()) {
                        continue;
                    }

                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();

                    // 只处理接口 + 带 @AgentService 注解
                    if (metadataReader.getClassMetadata().isInterface()) {
                        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                        if (annotationMetadata.hasAnnotation(AgentService.class.getName())) {
                            try {
                                Class<?> clazz = Class.forName(className);
                                log.info("Registering agent service interface: {}", clazz.getName());
                                result.add(clazz);
                            } catch (ClassNotFoundException e) {
                                log.warn("Failed to load class: {}", className, e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("I/O error while scanning package: {}", basePackage, e);
            }
        }
        return result;
    }

    private void registerAgentServiceBean(Class<?> mapperClass, BeanDefinitionRegistry registry) {
        String beanName = StringUtils.uncapitalize(mapperClass.getSimpleName());
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AgentServiceFactoryBean.class);
        builder.addPropertyValue("mapperClass", mapperClass);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        log.info("Registered agent service bean: {}", beanName);
    }
}
