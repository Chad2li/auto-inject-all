package io.github.chad2li.autoinject.core.util;

import io.github.chad2li.autoinject.core.cst.InjectCst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * el工具
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/1/22 22:50
 */
@Slf4j
public class ElUtil {

    public static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    public static boolean matchEl(@Nullable Map<?, ?> map, String el, @Nullable String value) {
        boolean result;
        if (StrUtil.isEmpty(el)) {
            result = true;
        } else {
            Object realValue = parseByEl(map, el);
            if (null == value) {
                // 如果期望值为null，则只要有该表达式即可
                result = null != realValue;
            } else if (null == realValue) {
                result = false;
            } else {
                result = value.equals(String.valueOf(realValue));
            }
        }
        log.info("match el, el:{}, value:{}, result:{}", el, value, result);
        return result;
    }

    public static Object parseByEl(@Nullable Map map, String el) {
        try {
            if (CollUtil.isEmpty(map)) {
                // 无上下文
                return parseByEl(el);
            }

            // 有上下文
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            ctx.setRootObject(map);
            ctx.addPropertyAccessor(new MapAccessor());
            Expression parser = EXPRESSION_PARSER.parseExpression(el);
            return parser.getValue(ctx);
        } catch (Throwable t) {
            log.warn("parse el error, el:{}, msg:{}", el, t.getMessage());
            return null;
        }
    }

    public static Object parseByElObj(@Nullable Object obj, String el) {
        if (null == obj) {
            // 无上下文
            return parseByEl(null, el);
        }

        Map map = new HashMap<>(1);
        map.put(InjectCst.SPEL_VALUE_ROOT, obj);
        return parseByEl(map, el);
    }

    public static boolean matchEl(String el, @Nullable String value) {
        if (StrUtil.isEmpty(el)) {
            return true;
        }

        String realValue = parseByEl(el);
        if (null == value) {
            // 如果期望值为null，则只要有该表达式即可
            return null != realValue;
        }
        if (null == realValue) {
            return false;
        }
        return value.equals(realValue);

    }

    private static String parseByEl(String el) {
        Expression parser = EXPRESSION_PARSER.parseExpression(el);
        return parser.getValue(String.class);
    }

    private ElUtil() {
        // do nothing
    }
}
