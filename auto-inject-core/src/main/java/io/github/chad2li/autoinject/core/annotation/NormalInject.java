package io.github.chad2li.autoinject.core.annotation;

import io.github.chad2li.autoinject.core.cst.InjectCst;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 普通的字值属性
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 12:21
 */
@Inject(strategy = InjectCst.NORMAL_STRATEGY, targetFieldName = InjectCst.DEFAULT_TARGET_FIELD_NAME)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NormalInject {
    /**
     * 将值注入的属性名 <br/>
     * 由 {@link Inject#targetFieldName()} 值决定
     */
    String targetField() default "";
}
