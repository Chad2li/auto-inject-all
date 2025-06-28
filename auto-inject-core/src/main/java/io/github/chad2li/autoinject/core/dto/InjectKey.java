package io.github.chad2li.autoinject.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * 属性标的注解和值
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 00:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InjectKey<A extends Annotation, K> {
    /**
     * 注解
     */
    private A anno;
    /**
     * 所在对象
     */
    private Object obj;
    /**
     * 来源属性值
     */
    private Object fromFieldValue;
    /**
     * 所在属性
     */
    private Field targetField;
    /**
     * 注解的值
     */
    private Set<K> idSet;
}
