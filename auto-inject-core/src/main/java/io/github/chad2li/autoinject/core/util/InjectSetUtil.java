package io.github.chad2li.autoinject.core.util;

import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.strategy.KeyFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 注入设置
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/22 16:07
 */
@Slf4j
public class InjectSetUtil {
    public static  void setInjectValue(List<InjectKey> injectKeys, Map<Object,
            Object> valueMap, KeyFunction keyFunction) {
        if (ToolUtil.hasEmpty(injectKeys, valueMap)) {
            return;
        }
        for (InjectKey key : injectKeys) {
            try {
                InjectSetUtil.setItemValue(key, valueMap, keyFunction);
            } catch (Exception e) {
                log.error("auto inject set value error, {}.{}, value:{}",
                        ClassUtil.getClass(key.getObj()), key.getTargetField().getName(),
                        key.getFromFieldValue(), e);
            }
        }
    }

    /**
     * 设置 item 值
     *
     * @param injectKey   注入信息
     * @param valueMap    注入的key值对
     * @param keyFunction 生成key的方法
     * @author chad
     * @since 1 by chad at 2025/5/22
     */
    public static void setItemValue(InjectKey injectKey, Map<Object, Object> valueMap,
                                        KeyFunction keyFunction) {
        Object injectValue;
        // 获取值
        Object fromFieldValue = injectKey.getFromFieldValue();
        if (ToolUtil.hasEmpty(valueMap, fromFieldValue)) {
            return;
        }
        Object subValue;
        Object key;
        if (fromFieldValue instanceof Collection<?>) {
            // 集合
            Collection<Object> idColl = (Collection<Object>) fromFieldValue;
            Collection<Object> resultColl = fromFieldValue instanceof Set<?> ? new HashSet<>(idColl.size())
                    : new ArrayList<>(idColl.size());
            for (Object id : idColl) {
                key = keyFunction.key(injectKey, id);
                subValue = valueMap.get(key);
                if (ToolUtil.isNotEmpty(subValue)) {
                    // spel解析
                    subValue = InjectSetUtil.getSpelValue(subValue, injectKey.getAnno());
                    resultColl.add(subValue);
                }
            }
            injectValue = resultColl;
        } else if (fromFieldValue instanceof Map<?, ?>) {
            // map
            Collection<Object> idColl = ((Map<Object, Object>) fromFieldValue).values();
            Map<Object, Object> resultMap = new HashMap<>(idColl.size());
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) fromFieldValue).entrySet()) {
                key = keyFunction.key(injectKey, entry.getValue());
                subValue = valueMap.get(key);
                if (ToolUtil.isNotEmpty(subValue)) {
                    // spel解析
                    subValue = InjectSetUtil.getSpelValue(subValue, injectKey.getAnno());
                    resultMap.put(entry.getKey(), subValue);
                }
            }
            injectValue = resultMap;
        } else {
            // 简单类型值
            key = keyFunction.key(injectKey, fromFieldValue);
            injectValue = valueMap.get(key);
            if (ToolUtil.isNotEmpty(injectValue)) {
                // spel解析
                injectValue = InjectSetUtil.getSpelValue(injectValue, injectKey.getAnno());
            }
        }

        if (null == injectValue) {
            log.info("not found inject value, {}.{}, value:{}", ClassUtil.getClass(injectKey.getObj()),
                    injectKey.getTargetField().getName(), injectKey.getFromFieldValue());
            return;
        }

        ReflectUtil.setFieldValue(injectKey.getObj(), injectKey.getTargetField(), injectValue);
    }

    private static Object getSpelValue(Object fromValue, Annotation anno) {
        if (null == fromValue) {
            return null;
        }
        String spel = InjectUtil.getTargetSpel(anno);
        if (StrUtil.isEmpty(spel)) {
            // 没有spel，使用 fromValue
            return fromValue;
        }
        // 解析spel
        return ElUtil.parseByElObj(fromValue, spel);
    }

    private InjectSetUtil() {
        // do nothing
    }
}
