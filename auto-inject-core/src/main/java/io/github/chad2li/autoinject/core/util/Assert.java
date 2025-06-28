package io.github.chad2li.autoinject.core.util;

import org.springframework.lang.Nullable;

/**
 * Assert
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/22 18:25
 */
public class Assert {

    public static void notNull(@Nullable Object object) {
        notNull(object, "cannot be null");
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(@Nullable Object object) {
        notEmpty(object, "cannot be empty");
    }

    public static void notEmpty(@Nullable Object object, String message) {
        if (ToolUtil.isEmpty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    private Assert() {
        // do nothing
    }
}
