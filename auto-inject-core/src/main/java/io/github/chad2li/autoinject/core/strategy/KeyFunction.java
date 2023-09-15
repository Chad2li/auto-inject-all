package io.github.chad2li.autoinject.core.strategy;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 注入值转map key函数
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 12:55
 */
public interface KeyFunction<A extends Annotation, Id, Key> {
    /**
     * 根据注解及ID生成对应的key，默认为id
     *
     * @param anno  注解
     * @param id    注解属性的id值
     * @param inObj 当前id属性所在的对象，用于获取对象其他值
     * @return 需要与 {@link AutoInjectStrategy#list(List)} 返回值的key对应，默认为 {@code id}
     * @author chad
     * @since 1 by chad at 2023/9/14
     */
    @SuppressWarnings("unchecked")
    default Key key(A anno, Id id, Object inObj) {
        return (Key) id;
    }
}
