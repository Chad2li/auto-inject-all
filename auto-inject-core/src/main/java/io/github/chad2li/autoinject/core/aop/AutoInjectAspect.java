package io.github.chad2li.autoinject.core.aop;

import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import io.github.chad2li.autoinject.core.util.AutoInjectUtil;
import io.github.chad2li.autoinject.core.util.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 自动注入拦截器
 *
 * @author chad
 * @date 2022/5/18 19:35
 * @since 1 create by chad
 */
@Slf4j
@Aspect
public class AutoInjectAspect {
    /**
     * springBean注入名称
     */
    public static final String SPRING_BEAN_NAME = InjectCst.SPRING_BEAN_NAME_PREFIX + "AutoInjectAspect";
    /**
     * spring aop 执行顺序
     */
    public static final int AOP_ORDER_SEQ = 1024;

    private Map<String, AutoInjectStrategy<?, ?, ?, ?>> strategyHandlerMap;

    public AutoInjectAspect(Set<AutoInjectStrategy<?, ?, ?, ?>> strategyHandlerSet) {
        if (CollUtil.isEmpty(strategyHandlerSet)) {
            log.warn("load strategy handler empty");
            strategyHandlerMap = Collections.emptyMap();
            return;
        }
        strategyHandlerMap = new HashMap<>(strategyHandlerSet.size());
        AutoInjectStrategy<?, ?, ?, ?> exists;
        String strategy;
        for (AutoInjectStrategy<?, ?, ?, ?> handler : strategyHandlerSet) {
            strategy = handler.strategy();
            exists = strategyHandlerMap.get(strategy);
            strategyHandlerMap.put(strategy, handler);
            if (null != exists) {
                log.info("load strategy handler will override, strategy:{}, from:{}, to:{}",
                        strategy, exists.getClass().getName(), handler.getClass().getName());
            } else {
                log.info("load strategy handler, strategy:{}, handler:{}", strategy,
                        handler.getClass().getName());
            }
        }
    }

    /**
     * 自动解析并注入字典值
     * <p>
     * 1. 响应是否有 field 有 {@code DictId} 注解<br/>
     * 2. 解析字典id和类型<br/>
     * 3. 调用实现类，获取字典项<br/>
     * 4. 设置值<br/>
     * </p>
     *
     * @param result 方法响应结果
     */
    @AfterReturning(value = "@annotation(io.github.chad2li.autoinject.core.annotation.InjectResult)",
            returning = "result")
    public void afterReturning(Object result) {
        // 处理注入
        AutoInjectUtil.inject(result, it -> this.strategyHandler(it));
    }

    @SuppressWarnings("unchecked")
    public AutoInjectStrategy<?, ?, ?, ?> strategyHandler(
            String strategy) {
        AutoInjectStrategy<?, ?, ?, ?> handler = strategyHandlerMap.get(strategy);
        if (null == handler) {
            throw new NullPointerException("not found inject handler, strategy:" + strategy);
        }
        return handler;
    }
}
