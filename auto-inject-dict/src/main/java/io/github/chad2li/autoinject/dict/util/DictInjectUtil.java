package io.github.chad2li.autoinject.dict.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.chad2li.autoinject.core.properties.DictAutoProperties;
import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import io.github.chad2li.autoinject.dict.dto.DictItem;
import org.springframework.lang.Nullable;

/**
 * TODO-类说明
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 08:54
 */
public class DictInjectUtil {
    /**
     * 解析 parentId
     *
     * @param dictObj    当前 dictId 所属的对象，注意并不是 DictId 注解标注的属性，是属性所在的对象
     * @param injectDict DictId注解
     * @return parentId or null if not parent
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    @Nullable
    public static String parseParentId(Object dictObj, InjectDict injectDict,
                                       DictAutoProperties dictProps) {
        if (ObjectUtil.hasEmpty(dictObj, injectDict)) {
            return null;
        }
        if (CharSequenceUtil.isNotEmpty(injectDict.parent())) {
            // parent优先级高于parentField
            return injectDict.parent();
        }
        String parentFieldName = injectDict.parentField();
        if (CharSequenceUtil.isEmpty(parentFieldName)) {
            // parent和parentField都无值，则返回配置值
            return dictProps.getDefaultParentId();
        }
        Object parentId = ReflectUtil.getFieldValue(dictObj, parentFieldName);
        if (null == parentId) {
            return null;
        }
        return String.valueOf(parentId).trim();
    }

    /**
     * 生成字典key
     *
     * @param dict dict dto
     * @return dict key
     * @author chad
     * @see DictInjectUtil#dictKey(String, Object, Object)
     * @since 1 by chad at 2023/9/14
     */
    public static <I> String dictKey(DictItem<I> dict) {
        return dictKey(dict.getType(), dict.getParentId(), dict.getId());
    }

    /**
     * 拼接字典key，用于快捷获取字典值
     *
     * @param type     dict type
     * @param parentId dict parent id
     * @param dictId   dict id
     * @return [type/][parentId/]id
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    public static String dictKey(String type, Object parentId, Object dictId) {
        String dictKey = "";
        // type
        type = null != type ? type.trim() : "";
        if (!type.isEmpty()) {
            dictKey += type + "/";
        }
        // parentId
        String parentIdStr = null != parentId ? String.valueOf(parentId).trim() : "";
        if (!parentIdStr.isEmpty()) {
            dictKey += parentIdStr + "/";
        }
        // id
        return dictKey + String.valueOf(dictId).trim();
    }

    private DictInjectUtil() {
        // do nothing
    }
}
