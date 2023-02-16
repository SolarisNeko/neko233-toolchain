package com.neko233.toolchain.aop;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author SolarisNeko on 2022-01-01
 */
@Slf4j
public class AopProxy233 {

    public static <T> T dynamicProxy(T target, AopApi aopApi) {
        // 定义一个类加载器
        ClassLoader loader = target.getClass().getClassLoader();
        // 定义一个接口数组
        Class<?>[] interfaces = target.getClass().getInterfaces();
        // 定义一个 InvocationHandler
        InvocationHandler handler = (proxy, method, args) -> {
            aopApi.preHandle(method, target, args);
            Object returnValue = null;
            int retryCount = Math.max(0, aopApi.retryCountOnError());
            for (int tryCount = 0; tryCount < 1 + retryCount; tryCount++) {
                try {
                    returnValue = method.invoke(target, args);
                    break;
                } catch (Exception e) {
                    boolean isEat = aopApi.tryEatException(e);
                    if (isEat) {
                        continue;
                    }
                    throw e.getCause();
                }
            }
            aopApi.postHandle(method, target, args);
            return returnValue;
        };
        return (T) Proxy.newProxyInstance(loader, interfaces, handler);
    }
}