package io.github.chad2li.autoinject.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标明返回值需要执行自动注入程序
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/25 01:54
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectResult {

}
