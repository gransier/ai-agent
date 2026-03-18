package cn.gransier.common.annotation;

import cn.gransier.common.enums.AgentMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AgentMethod {
    String endpoint();
    AgentMethods method();
    String contentType() default "application/json";
}
