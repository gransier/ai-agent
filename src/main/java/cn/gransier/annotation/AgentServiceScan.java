package cn.gransier.annotation;

import org.springframework.context.annotation.Import;
import cn.gransier.config.AgentServiceScannerRegistrar;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AgentServiceScannerRegistrar.class)
public @interface AgentServiceScan {
    String[] value() default {};
}
