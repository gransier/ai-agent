package cn.gransier.common.tool.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {
    String name();

    String description();

    boolean async() default true;
}
