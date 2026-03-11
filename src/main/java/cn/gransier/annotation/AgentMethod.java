package cn.gransier.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AgentMethod {
    String apiKey();
    String endpoint();
    String responseKey() default "answer";
}
