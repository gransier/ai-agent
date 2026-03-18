package cn.gransier;

import cn.gransier.common.util.TypeUtils;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectTest {

    // 模拟你的接口方法
    public Flux<Double> completions(DifyChatQuery difyQuery) {
        return null;
    }

    public static void main(String[] args) throws NoSuchMethodException {
        // 1. 获取 Method 对象
        Method method = ReflectTest.class.getMethod("completions", DifyChatQuery.class);

        // 2. 获取泛型返回类型 (注意：不是 getReturnType())
        Class<?> clazz = TypeUtils.getGenericReturnType(method, 0);

        // 输出结果
        System.out.println("泛型类型: " + clazz);                       // 输出: class java.lang.String
    }


    public static class DifyChatQuery {
    }

}
