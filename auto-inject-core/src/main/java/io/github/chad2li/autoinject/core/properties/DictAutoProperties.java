package io.github.chad2li.autoinject.core.properties;

import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.util.StrUtil;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/25 20:39
 */
@Getter
@ConfigurationProperties(prefix = "dict-auto")
public class DictAutoProperties {
    /**
     * 默认的 parentId 值，通常为根级id或code
     */
    private String defaultParentId;
    /**
     * 默认的 dictId 属性名后缀，用于计算被注入字典值的属性名
     */
    private String dictIdSuffix;
    /**
     * 默认的 dictItem 属性名后缀，用于计算被注入字典值的属性名
     * 如 dictIdSuffix = DictId, dictItemSuffix = DictItem，
     * genderDictId 的字典值名称则为 genderDictItem
     */
    private String dictItemSuffix;

    public void setDefaultParentId(String defaultParentId) {
        if (StrUtil.isEmpty(defaultParentId)) {
            return;
        }
        this.defaultParentId = defaultParentId;
    }

    public void setDictIdSuffix(String dictIdSuffix) {
        if (StrUtil.isEmpty(dictIdSuffix)) {
            return;
        }
        this.dictIdSuffix = dictIdSuffix;
    }

    public void setDictItemSuffix(String dictItemSuffix) {
        if (StrUtil.isEmpty(dictItemSuffix)) {
            return;
        }
        this.dictItemSuffix = dictItemSuffix;
    }

    public DictAutoProperties() {
        this.defaultParentId = InjectCst.DEFAULT_PARENT_ID;
        this.dictIdSuffix = InjectCst.FIELD_DICT_ID_SUFFIX;
        this.dictItemSuffix = InjectCst.FIELD_DICT_ITEM_SUFFIX;
    }
}
