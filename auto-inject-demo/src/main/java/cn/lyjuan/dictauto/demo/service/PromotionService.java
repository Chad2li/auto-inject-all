package cn.lyjuan.dictauto.demo.service;

import cn.hutool.core.collection.CollUtil;
import cn.lyjuan.dictauto.demo.annotation.InjectPromotionName;
import cn.lyjuan.dictauto.demo.consts.DemoConst;
import cn.lyjuan.dictauto.demo.vo.PromotionVo;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动服务
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/16 10:24
 */
@Service
public class PromotionService implements AutoInjectStrategy<Long, Long, PromotionVo, InjectPromotionName> {
    @Override
    public String strategy() {
        return DemoConst.PROMOTION_NAME;
    }

    @Override
    public Map<Long, PromotionVo> list(List<InjectKey<InjectPromotionName, Long>> injectKeys) {
        if (CollUtil.isEmpty(injectKeys)) {
            return Collections.emptyMap();
        }
        Set<Long> idSet = injectKeys.stream()
                .flatMap(it -> it.getIdSet().stream())
                .collect(Collectors.toSet());
        // 移除无效的值
        idSet.remove(0L);
        idSet.remove(null);
        if (CollUtil.isEmpty(idSet)) {
            return Collections.emptyMap();
        }
        // 这里是mock的值，实际情况应该是查数据库或缓存
        Map<Long, PromotionVo> promotionIdNameMap = new HashMap<>(idSet.size());
        for (Long id : idSet) {
            promotionIdNameMap.put(id, new PromotionVo(id, "这是一个测试活动-" + id));
        }
        return promotionIdNameMap;
    }
}
