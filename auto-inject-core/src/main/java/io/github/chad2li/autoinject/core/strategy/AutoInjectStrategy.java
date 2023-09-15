package io.github.chad2li.autoinject.core.strategy;

import io.github.chad2li.autoinject.core.dto.InjectKey;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

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
     * 查询对应的id的值
     *
     * @param injectKeyList 注解及id
     * @return dict list
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    Map<Key, Value> list(List<InjectKey<A, Id>> injectKeyList);
}
