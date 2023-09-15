package io.github.chad2li.autoinject.core.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具
 *
 * @author chad
 * @date 2022/5/18 23:57
 * @see ReflectUtil
 * @since 1 create by chad
 */
public class InjectReflectUtil extends ReflectUtil {
    /**
     * 获取有 {@code get, is, has} 方法的属性，并向上层类中查找
     *
     * @param beanClass            查找的类
     * @param withSuperClassFields 是否向上层类查找，true是
     * @return 带有 public 无参{@code get, is, has}方法的属性
     * @date 2022/5/19 00:39
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static Field[] getFieldsDirectlyHasGetter(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        Assert.notNull(beanClass);
        List<Field> fieldList = new ArrayList<>();

        for (Class searchType = beanClass; searchType != null; searchType = withSuperClassFields ? searchType.getSuperclass() : null) {
            Field[] allFields = searchType.getDeclaredFields();
            if (ArrayUtil.isEmpty(allFields)) {
                continue;
            }
            // 去掉没有 getter 方法的 field
            for (Field f : allFields) {
                String fieldNameFirstUp = f.getName();
                if (fieldNameFirstUp.length() > 1) {
                    fieldNameFirstUp = fieldNameFirstUp.substring(0, 1).toUpperCase() + fieldNameFirstUp.substring(1);
                } else {
                    fieldNameFirstUp = fieldNameFirstUp.toUpperCase();
                }

                String methodName = "get" + fieldNameFirstUp;
                boolean has = null != ReflectUtil.getPublicMethod(searchType, methodName, null);
                if (!has) {
                    methodName = "is" + fieldNameFirstUp;
                    has = null != ReflectUtil.getPublicMethod(searchType, methodName, null);
                }

                if (!has) {
                    methodName = "has" + fieldNameFirstUp;
                    has = null != ReflectUtil.getPublicMethod(searchType, methodName, null);
                }

                if (!has) {
                    // 没有 getter 或 is 获取属性的 public 无参方法
                    continue;
                }
                fieldList.add(f);
            }
        }

        if (CollectionUtil.isEmpty(fieldList)) {
            return new Field[0];
        }
        return fieldList.toArray(new Field[0]);
    }
}
