package cn.gransier.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtils {

    @SuppressWarnings("all")
    public static Class getGenericReturnType(Method method, int order) {
        Type genericReturnType = method.getGenericReturnType();

        // 判断是否为参数化类型 (即是否有尖括号 <T>)
        if (genericReturnType instanceof ParameterizedType returnType) {
            // 获取实际类型参数数组 (例如 Flux<String> 中的 String)
            Type[] actualTypeArguments = returnType.getActualTypeArguments();
            if (actualTypeArguments.length > order) {
                Type type = actualTypeArguments[order];
                if (type instanceof Class<?> clazz){
                    return clazz;
                }
            }
        }
        throw new RuntimeException("该方法返回类型不是参数化类型");
    }
}
