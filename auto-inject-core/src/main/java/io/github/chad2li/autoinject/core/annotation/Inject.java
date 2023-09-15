package io.github.chad2li.autoinject.core.annotation;

import io.github.chad2li.autoinject.core.cst.InjectCst;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入的基础注解，可扩展自定义属性
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/12 08:37
 */
@Inherited
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inject {
    /**
     * 策略
     */
    String strategy() default "";

    /**
     * 注入目标属性名称
     */
    String targetFieldName() default InjectCst.DEFAULT_TARGET_FIELD_NAME;
}
