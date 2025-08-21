package io.github.chad2li.autoinject.core.util;

import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * file id util
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/25 09:05
 */
@Slf4j
public class AutoInjectUtil {

    /**
     * 自动注入处理
     *
     * @param obj             对象
     * @param strategyFactory 策略处理器工厂，根据策略名称获取策略处理器
     * @author chad
     * @since 1 by chad at 2025/5/22
     */
    public static void inject(Object obj, Function<String, AutoInjectStrategy> strategyFactory) {
        // 1. 获取所有包含inject注解
        List<InjectKey> injectKeys = InjectQueryUtil.queryDictAnnotation(obj);
        if (CollUtil.isEmpty(injectKeys)) {
            return;
        }
        // 2. 对注解按 strategy 分组, key: strategy
        Map<String, List<InjectKey>> group = injectKeys.stream().collect(
                Collectors.groupingBy(it -> InjectUtil.strategy(it.getAnno())));
        // 3. 遍历分组
        for (Map.Entry<String, List<InjectKey>> entry : group.entrySet()) {
            // 3.1 获取 strategy 对应的 handler
            AutoInjectStrategy<Object, Object, Object, Annotation> handler =
                    strategyFactory.apply(entry.getKey());
            // 3.2 获取分组下所有的 id
            // 3.3 分批查询 id 对应的值 map.key: id, map.value: 注入的值
            Map<Object, Object> value = handler.list((List)entry.getValue());
            // 3.3 注入
            InjectSetUtil.setInjectValue((List) entry.getValue(), value, handler);
        }
    }


    private AutoInjectUtil() {
        // do nothing
    }
}
