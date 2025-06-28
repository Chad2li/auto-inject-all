package io.github.chad2li.autoinject.core.util;


import java.util.Iterator;
import java.util.Map;

/**
 * 拷贝的 hutool 不兼容工具方法
 *
 * @author chad
 * @copyright 2024 chad
 * @since created at 2024/11/21 14:16
 */
public class ToolUtil {
    public static boolean isNotEmpty(Object... objects) {
        return !isEmpty(objects);
    }


    public static boolean isAllNotEmpty(Object... objects) {
        return !hasEmpty(objects);
    }


    /**
     * 是否存在{@code null}或空对象
     *
     * @param args 被检查对象
     * @return 是否存在
     * @since 4.5.18
     */
    public static boolean hasEmpty(Object... args) {
        if (isNotEmpty(args)) {
            for (Object element : args) {
                if (isEmpty(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }

        if (obj instanceof String) {
            return StrUtil.isEmpty((String) obj);
        } else if (obj instanceof Map) {
            return CollUtil.isEmpty((Map) obj);
        } else if (obj instanceof Iterable) {
            return CollUtil.isEmpty((Iterable) obj);
        } else if (obj instanceof Iterator) {
            return CollUtil.isEmpty((Iterator) obj);
        } else if (ArrayUtil.isArray(obj)) {
            return ArrayUtil.isEmpty(obj);
        }

        return false;
    }

    private ToolUtil() {
        // do nothing
    }
}
