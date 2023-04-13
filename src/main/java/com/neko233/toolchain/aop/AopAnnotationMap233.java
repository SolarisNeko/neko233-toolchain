package com.neko233.toolchain.aop;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SolarisNeko
 * Date on 2023-04-13
 */
public class AopAnnotationMap233 {

    private static final Map<Annotation, AopApi> ANNOTATION_AOP_API_MAP = new ConcurrentHashMap<>();

    public static AopApi get(Annotation annotation) {
        return ANNOTATION_AOP_API_MAP.get(annotation);
    }

    public static AopApi register(Annotation annotation, AopApi api) {
        return ANNOTATION_AOP_API_MAP.put(annotation, api);
    }

}
