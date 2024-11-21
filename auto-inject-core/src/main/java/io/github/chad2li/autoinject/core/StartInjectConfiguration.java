package io.github.chad2li.autoinject.core;

import io.github.chad2li.autoinject.core.aop.AutoInjectAspect;
import io.github.chad2li.autoinject.core.properties.DictAutoProperties;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Set;

/**
 * 自动配置 Dict 注入
 *
 * @author chad
 * @since 1 create by chad at 2022/5/19 12:34
 */
@EnableAspectJAutoProxy
@EnableConfigurationProperties(DictAutoProperties.class)
public class StartInjectConfiguration {
    @Bean(AutoInjectAspect.SPRING_BEAN_NAME)
    public AutoInjectAspect dictAopHandler(Set<AutoInjectStrategy<?, ?, ?, ?>> strategyHandlerSet) {
        return new AutoInjectAspect(strategyHandlerSet);
    }
}
