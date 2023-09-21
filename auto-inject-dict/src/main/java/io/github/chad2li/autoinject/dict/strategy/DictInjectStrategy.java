package io.github.chad2li.autoinject.dict.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.properties.DictAutoProperties;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import io.github.chad2li.autoinject.dict.cst.DictCst;
import io.github.chad2li.autoinject.dict.dto.DictItem;
import io.github.chad2li.autoinject.dict.util.DictInjectUtil;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字典注入策略
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 08:27
 */
public abstract class DictInjectStrategy<Id>
        implements AutoInjectStrategy<String, Id, DictItem<Id>, InjectDict> {
    @Resource
    private DictAutoProperties dictProps;

    @Override
    public String strategy() {
        return DictCst.DICT;
    }

    @Override
    public String key(InjectDict anno, Id id, Object inObj) {
        if (ObjectUtil.hasEmpty(anno, id, inObj)) {
            return "";
        }
        // 获取parentId
        String parentId = DictInjectUtil.parseParentId(inObj, anno, dictProps);
        // 根据字典3要素生成 key
        return DictInjectUtil.dictKey(anno.type(), parentId, id);
    }


    @Override
    public Map<String, DictItem<Id>> list(List<InjectKey<InjectDict, Id>> injectKeys) {
        if (CollUtil.isEmpty(injectKeys)) {
            return Collections.emptyMap();
        }
        // 1. 获取所有type
        Set<String> typeSet = new HashSet<>(injectKeys.size());
        for (InjectKey<InjectDict, Id> injectKey : injectKeys) {
            typeSet.add(injectKey.getAnno().type());
        }
        // 2. 查询 type
        List<DictItem<Id>> valueList = this.list(typeSet.toArray(new String[0]));
        // 3. 转 map
        return valueList.stream().collect(Collectors.toMap(it -> DictInjectUtil.dictKey(it.getType(),
                it.getParentId(), it.getId()), Function.identity()));
    }

    /**
     * 批量查询type下的字典值
     *
     * @param type 字典类型
     * @return dict list
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    public abstract List<DictItem<Id>> list(String... type);
}
