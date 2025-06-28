package io.github.chad2li.autoinject.core.annotation;

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
     * 注入值的来源属性
     */
    String fromField() default "";

    /**
     * 提取值的ep表达式
     * <pre>
     *     策略返回值作为上下文，如果spel为空，则直接使用返回值
     * </pre>
     */
    String targetSpel() default "";
}
