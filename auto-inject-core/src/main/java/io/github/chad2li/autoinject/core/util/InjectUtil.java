package io.github.chad2li.autoinject.core.util;

import io.github.chad2li.autoinject.core.annotation.Inject;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;

/**
 * inject通用工具
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/22 16:17
 */
public class InjectUtil {
    public static boolean isInject(Annotation annotation) {
        return annotation instanceof Inject;
    }

    public static boolean isInjectChild(Annotation annotation) {
        return null != annotation.annotationType().getAnnotation(Inject.class);
    }

    public static Inject getInjectFromChild(Annotation annotation) {
        return annotation.annotationType().getAnnotation(Inject.class);
    }

    /**
     * 获取注解上的策略
     *
     * @param annotation 注解Inject或其子类o
     * @return 策略，如果无则为null
     * @author chad
     * @since 1 by chad at 2023/9/20
     */
    @Nullable
    public static String strategy(@Nullable Annotation annotation) {
        if (null == annotation) {
            return null;
        }
        Inject inject;
        if (isInject(annotation)) {
            inject = (Inject) annotation;
        } else {
            inject = getInjectFromChild(annotation);
        }
        return inject.strategy();
    }


    /**
     * 获取targetSpel
     *
     * @param anno 注解
     * @return 可能为null
     * @author chad
     * @since 1 by chad at 2025/5/22
     */
    @Nullable
    public static String getTargetSpel(Annotation anno) {
        String targetSpel;
        if (anno instanceof Inject) {
            // 本身就是 inject
            targetSpel = ((Inject) anno).targetSpel();
        } else {
            // 不是 inject，反射获取
            targetSpel = ReflectUtil.invokeDefaultNull(anno, InjectCst.TARGET_SPEL_NAME);
        }
        return StrUtil.isNotEmpty(targetSpel) ? targetSpel.trim() : null;
    }

    private InjectUtil() {
        // do nothing
    }
}
