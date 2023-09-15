package io.github.chad2li.autoinject.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
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
public class InjectKey<A extends Annotation, T> {
    /**
     * 注解
     */
    private A anno;

    /**
     * 注解的值
     */
    private Set<T> idSet;
}
