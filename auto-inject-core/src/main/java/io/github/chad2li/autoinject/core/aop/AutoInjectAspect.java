package io.github.chad2li.autoinject.core.aop;

import cn.hutool.core.collection.CollUtil;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import io.github.chad2li.autoinject.core.util.AutoInjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自动注入拦截器
 *
 * @author chad
 * @date 2022/5/18 19:35
 * @since 1 create by chad
 */
@Slf4j
@Aspect
@Order(AutoInjectAspect.AOP_ORDER_SEQ)
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
        // 1. 获取所有包含inject注解
        Set<InjectKey<Annotation, Object>> injectKeys = AutoInjectUtil.queryDictAnnotation(result);
        if (CollUtil.isEmpty(injectKeys)) {
            return;
        }
        // 2. 对注解按 strategy 分组, key: strategy
        Map<String, List<InjectKey<Annotation, Object>>> group = injectKeys.stream().collect(
                Collectors.groupingBy(it -> AutoInjectUtil.strategy(it.getAnno())));
        // 3. 遍历分组
        for (Map.Entry<String, List<InjectKey<Annotation, Object>>> entry : group.entrySet()) {
            // 3.1 获取 strategy 对应的 handler
            AutoInjectStrategy<Object, Object, Object, Annotation> handler =
                    strategyHandler(entry.getKey());
            // 3.2 获取分组下所有的 id
            // 3.3 分批查询 id 对应的值 map.key: id, map.value: 注入的值
            Map<Object, Object> value = handler.list(entry.getValue());
            // 3.3 注入
            AutoInjectUtil.injectionDict(entry.getKey(), result, value, handler);
        }
    }

    @SuppressWarnings("unchecked")
    public <Key, Id, Value, A extends Annotation> AutoInjectStrategy<Key, Id, Value, A> strategyHandler(
            String strategy) {
        AutoInjectStrategy<?, ?, ?, ?> handler = strategyHandlerMap.get(strategy);
        if (null == handler) {
            throw new NullPointerException("not found inject handler, strategy:" + strategy);
        }
        return (AutoInjectStrategy<Key, Id, Value, A>) handler;
    }
}
