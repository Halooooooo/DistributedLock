package com.halo.lock.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author: Halo
 * @date: 2019/7/2 9:38
 * @Description:
 */
public class AnnotationResolver {
    public static Object getAnnotation(JoinPoint jp ,Class clazz){
        Class declaringType = jp.getSignature().getDeclaringType();
        Method method = getMethod(jp, declaringType);
        return  method.getAnnotation(clazz);
    }

    private static Method getMethod(JoinPoint jp, Class declaringType) {
        Signature signature = jp.getSignature();
        MethodSignature msig;
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) signature;
        return ReflectionUtils.findMethod(declaringType, signature.getName(), msig.getParameterTypes());
    }
}
