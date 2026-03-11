package cn.gransier.config;

import cn.gransier.common.DefaultDifyStreamListener;
import cn.gransier.domain.query.AgentQuery;
import cn.gransier.util.DifyClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AgentServiceFactoryBean implements FactoryBean<Object>, InvocationHandler, BeanFactoryAware {

    private Class<?> mapperClass;
    private BeanFactory beanFactory;

    public void setMapperClass(Class<?> mapperClass) {
        this.mapperClass = mapperClass;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private DifyClient getDifyClient() {
        return beanFactory.getBean(DifyClient.class);
    }

    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(
                mapperClass.getClassLoader(),
                new Class[]{mapperClass},
                this
        );
    }

    @Override
    public Class<?> getObjectType() {
        return mapperClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            if ("toString".equals(method.getName())) {
                return "AgentServiceProxy@" + System.identityHashCode(proxy);
            } else if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            } else if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
        }

        String methodName = method.getName();
        if ("completions".equals(methodName)) {
            return handleCompletions(method, args);
        }
        
        System.out.println("AgentService代理: 调用方法 " + methodName);
        return null;
    }

    private Flux<String> handleCompletions(Method method, Object[] args) {
        AgentQuery agentQuery = (AgentQuery) args[0];
        DifyClient difyClient = getDifyClient();
        return Flux.create(sink -> difyClient.stream(
                "app-gItXxmtOzXp7S6TdJb7TNcdF",
                "/v1/chat-messages",
                agentQuery,
                DefaultDifyStreamListener.newInstance(sink)
        ));
    }
}
