package cn.lyjuan.dictauto.demo.annotation;

import cn.lyjuan.dictauto.demo.consts.DemoConst;
import io.github.chad2li.autoinject.core.annotation.Inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 普通的字值属性
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 12:21
 */
@Inject(strategy = DemoConst.PROMOTION_NAME)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectPromotionName {
    String fromField() default "";
    /**
     * 提取值的ep表达式
     * <pre>
     *     策略返回值作为上下文，如果spel为空，则直接使用返回值
     * </pre>
     */
    String targetSpel() default "";
}
