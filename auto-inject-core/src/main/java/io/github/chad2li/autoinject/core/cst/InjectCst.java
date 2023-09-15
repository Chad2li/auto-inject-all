package io.github.chad2li.autoinject.core.cst;

import io.github.chad2li.autoinject.core.annotation.Inject;

/**
 * 静态值
 *
 * @author chad
 * @date 2022/5/18 21:17
 * @since 1 create by chad
 */
public class InjectCst {
    /**
     * spring bean 注入名称前缀
     */
    public static final String SPRING_BEAN_NAME_PREFIX = "springBean";
    /**
     * 字典默认的 parentId
     */
    public static final String DEFAULT_PARENT_ID = "0";

    /**
     * 字典ID属性后缀
     */
    public static final String FIELD_DICT_ID_SUFFIX = "Id";
    /**
     * 字典项属性后缀
     */
    public static final String FIELD_DICT_ITEM_SUFFIX = "Item";
    /**
     * {@link Inject} 子注解的注入目标属性名称
     */
    public static final String DEFAULT_TARGET_FIELD_NAME = "targetField";
    /**
     * 普通的字值属性
     */
    public static final String NORMAL_STRATEGY = "normal";

}
