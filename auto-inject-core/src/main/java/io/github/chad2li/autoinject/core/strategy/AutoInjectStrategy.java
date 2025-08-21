package io.github.chad2li.autoinject.core.strategy;

import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.util.CollUtil;

import java.awt.image.ImagingOpException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取字典数据的服务
 *
 * @author chad
 * @since 1 create by chad at 2023/9/14
 */
public interface AutoInjectStrategy<Key, Id, Value, A extends Annotation>
        extends KeyFunction<A, Id, Key> {
    /**
     * 当前handler处理的策略
     *
     * @return 策略
     * @author chad
     * @since 1 by chad at 2023/9/14
     */
    String strategy();

    /**
     * 是否使用全参数查询
     * <pre>
     *     通常情况下只需要使用 {@link AutoInjectStrategy#list(Set)}
     * </pre>
     *
     * @return true 使用 {@link AutoInjectStrategy#list(List)}，否则使用 {@link AutoInjectStrategy#list(Set)}
     * @author chad
     * @since 1 by chad at 2025/5/22
     */
    default boolean useFullQuery() {
        return true;
    }

    /**
     * 查询对应的id的值
     *
     * @param keyList 注解及id
     * @return dict list
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    default Map<Key, Value> list(List<InjectKey<A, Id>> keyList) {
        if (CollUtil.isEmpty(keyList)) {
            return Collections.emptyMap();
        }

        if (this.useFullQuery()) {
            throw new IllegalStateException("not implements");
        }
        Set<Id> idSet = keyList.stream()
                .flatMap(it -> it.getIdSet().stream())
                .collect(Collectors.toSet());
        return this.list(idSet);
    }

    default Map<Key, Value> list(Set<Id> idSet) {
        throw new IllegalStateException("not implements");
    }
}
