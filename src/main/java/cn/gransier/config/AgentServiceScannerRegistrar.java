package cn.gransier.config;

import cn.gransier.annotation.AgentServiceScan;
import lombok.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AgentServiceScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        List<Class<?>> mapperClasses = getMapperClasses(importingClassMetadata);
        for (Class<?> mapperClass : mapperClasses) {
            registerMapperBeanDefinition(mapperClass, registry);
        }
    }

    private List<Class<?>> getMapperClasses(AnnotationMetadata metadata) {
        List<Class<?>> classes = new ArrayList<>();
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(AgentServiceScan.class.getName());
        assert annotationAttributes != null;
        String[] basePackages = (String[]) annotationAttributes.get("value");

        for (String basePackage : basePackages) {
            classes.addAll(scanPackage(basePackage));
        }
        return classes;
    }

    private List<Class<?>> scanPackage(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = basePackage.replace('.', '/');
        URL url = Thread.currentThread().getContextClassLoader().getResource(packagePath);
        if (url != null && "file".equals(url.getProtocol())) {
            File dir = new File(url.getPath());
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isDirectory()) {
                    classes.addAll(scanPackage(basePackage + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    try {
                        Class<?> clazz = Class.forName(basePackage + "." + file.getName().replace(".class", ""));
                        if (clazz.isInterface()) {
                            classes.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return classes;
    }

    private void registerMapperBeanDefinition(Class<?> mapperClass, BeanDefinitionRegistry registry) {
        String beanName = StringUtils.uncapitalize(mapperClass.getSimpleName());
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AgentServiceFactoryBean.class);
        builder.addPropertyValue("mapperClass", mapperClass);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}
