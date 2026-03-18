package cn.gransier.common.tool.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {
    String name() default "";

    boolean required() default false;

    String description() default "";

    String example() default "";

    String[] enums() default {};
}
