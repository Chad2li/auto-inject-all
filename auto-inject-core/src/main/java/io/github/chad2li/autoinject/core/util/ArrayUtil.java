package io.github.chad2li.autoinject.core.util;

import java.lang.reflect.Array;

/**
 * TODO-类说明
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/21 10:14
 */
public class ArrayUtil {

    public static <T> boolean isEmpty(T[] arr) {
        return null == arr || arr.length < 1;
    }

    public static <T> boolean isNotEmpty(T[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isEmpty(Object array) {
        if (array != null) {
            if (isArray(array)) {
                return 0 == Array.getLength(array);
            }
            return false;
        }
        return true;
    }

    public static boolean isNotEmpty(Object array) {
        return !isEmpty(array);
    }

    public static boolean isArray(Object obj) {
        return null != obj && obj.getClass().isArray();
    }

    public static int length(Object array) throws IllegalArgumentException {
        if (null == array) {
            return 0;
        }
        return Array.getLength(array);
    }
    private ArrayUtil() {
        // do nothing
    }
}
