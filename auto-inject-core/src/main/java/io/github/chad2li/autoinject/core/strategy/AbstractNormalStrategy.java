package io.github.chad2li.autoinject.core.strategy;

import io.github.chad2li.autoinject.core.annotation.InjectNormal;
import io.github.chad2li.autoinject.core.cst.InjectCst;

/**
 * Normal 值获取策略
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/16 09:53
 */
public abstract class AbstractNormalStrategy<MapK, Id, MapV>
        implements AutoInjectStrategy<MapK, Id, MapV, InjectNormal> {
    @Override
    public String strategy() {
        return InjectCst.NORMAL_STRATEGY;
    }
}
