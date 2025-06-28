package io.github.chad2li.autoinject.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO-类说明
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/21 13:57
 */
public class ClassUtil {

    public static <T> Class<T> getClass(T obj) {
        return ((null == obj) ? null : (Class<T>) obj.getClass());
    }

    public static Class<?>[] getClasses(Object... objects) {
        Class<?>[] classes = new Class<?>[objects.length];
        Object obj;
        for (int i = 0; i < objects.length; i++) {
            obj = objects[i];
            if (null == obj) {
                classes[i] = Object.class;
            } else {
                classes[i] = obj.getClass();
            }
        }
        return classes;
    }

    public static boolean isAllAssignableFrom(Class<?>[] types1, Class<?>[] types2) {
        if (ArrayUtil.isEmpty(types1) && ArrayUtil.isEmpty(types2)) {
            return true;
        }
        if (null == types1 || null == types2) {
            // 任何一个为null不相等（之前已判断两个都为null的情况）
            return false;
        }
        if (types1.length != types2.length) {
            return false;
        }

        Class<?> type1;
        Class<?> type2;
        for (int i = 0; i < types1.length; i++) {
            type1 = types1[i];
            type2 = types2[i];
            if (isBasicType(type1) && isBasicType(type2)) {
                // 原始类型和包装类型存在不一致情况
                if (BasicType.unWrap(type1) != BasicType.unWrap(type2)) {
                    return false;
                }
            } else if (false == type1.isAssignableFrom(type2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否为基本类型（包括包装类和原始类）
     *
     * @param clazz 类
     * @return 是否为基本类型
     */
    public static boolean isBasicType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * 是否为包装类型
     *
     * @param clazz 类
     * @return 是否为包装类型
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return BasicType.WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    /**
     * 获取指定类型分的默认值<br>
     * 默认值规则为：
     *
     * <pre>
     * 1、如果为原始类型，返回0
     * 2、非原始类型返回{@code null}
     * </pre>
     *
     * @param clazz 类
     * @return 默认值
     * @since 3.0.8
     */
    public static Object getDefaultValue(Class<?> clazz) {
        // 原始类型
        if (clazz.isPrimitive()) {
            return getPrimitiveDefaultValue(clazz);
        }
        return null;
    }

    public static Object getPrimitiveDefaultValue(Class<?> clazz) {
        if (long.class == clazz) {
            return 0L;
        } else if (int.class == clazz) {
            return 0;
        } else if (short.class == clazz) {
            return (short) 0;
        } else if (char.class == clazz) {
            return (char) 0;
        } else if (byte.class == clazz) {
            return (byte) 0;
        } else if (double.class == clazz) {
            return 0D;
        } else if (float.class == clazz) {
            return 0f;
        } else if (boolean.class == clazz) {
            return false;
        }
        return null;
    }

    /**
     * 检查目标类是否可以从原类转化<br>
     * 转化包括：<br>
     * 1、原类是对象，目标类型是原类型实现的接口<br>
     * 2、目标类型是原类型的父类<br>
     * 3、两者是原始类型或者包装类型（相互转换）
     *
     * @param targetType 目标类型
     * @param sourceType 原类型
     * @return 是否可转化
     */
    public static boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        if (null == targetType || null == sourceType) {
            return false;
        }

        // 对象类型
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        // 基本类型
        if (targetType.isPrimitive()) {
            // 原始类型
            Class<?> resolvedPrimitive = BasicType.WRAPPER_PRIMITIVE_MAP.get(sourceType);
            return targetType.equals(resolvedPrimitive);
        } else {
            // 包装类型
            Class<?> resolvedWrapper = BasicType.PRIMITIVE_WRAPPER_MAP.get(sourceType);
            return resolvedWrapper != null && targetType.isAssignableFrom(resolvedWrapper);
        }
    }

    private ClassUtil() {
        // do nothing
    }
}
