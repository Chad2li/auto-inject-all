package io.github.chad2li.autoinject.dict.annotation;


import io.github.chad2li.autoinject.core.annotation.Inject;
import io.github.chad2li.autoinject.dict.cst.DictCst;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chad
 * @since 1 by chad at 2022/5/13<br/>
 * 2 by chad at 2023/8/25: 增加的target
 */
@Inject(strategy = DictCst.DICT)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectDict {
    /**
     * 字典类型
     */
    String type();

    /**
     * 字典父级属性
     */
    String parentField() default "";

    /**
     * 来源属性名
     */
    String fromField() default "";

    /**
     * parent和parentField仅需填一个，parent优先级高于parentField
     * 仅支持 String 和 Long 类型
     */
    String parent() default "";
}
