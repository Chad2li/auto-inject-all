package io.github.chad2li.autoinject.core.util;

import io.github.chad2li.autoinject.core.annotation.Inject;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 注入查询
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/22 16:07
 */
@Slf4j
public class InjectQueryUtil {
    /**
     * 查询对象中所有FileId注解
     *
     * @param fileObj 对象
     * @return fileId
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static List<InjectKey> queryDictAnnotation(Object fileObj) {
        List<InjectKey> list = new LinkedList<>();
        injectionDict(fileObj, list);
        return list;
    }


    /**
     * 解析对象，将其中有 {@link Inject} 或有其子注解的属性，自动进行字典值注入
     *
     * @param currObj 对象
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static void injectionDict(@Nullable Object currObj, List<InjectKey> resultList) {
        if (null == currObj) {
            log.debug("Result is null");
            return;
        }
        if (isExists(resultList, currObj)) {
            // 不重复解析对象 - chad
            return;
        }
        if (currObj instanceof Iterable) {
            // iterable
            injectionIterable((Iterable<?>) currObj, resultList);
        } else if (currObj instanceof Map) {
            // map
            injectionMap((Map<?, ?>) currObj, resultList);
        } else {
            // other
            injectionObject(currObj, resultList);
        }
    }

    /**
     * 解析特定属性
     *
     * @param currObj   当前对象
     * @param currField 对象属性
     * @return 所有FileId注解
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    @Nullable
    private static void injectionDict(Object currObj, Field currField, List<InjectKey> resultList) {
        if (null == currObj) {
            return;
        }
        Annotation injectAnno = InjectQueryUtil.getInjectAnnotation(currField);
        // target field value
        Object currFieldValue = InjectQueryUtil.getTargetFieldValue(currObj, currField);
        if (null == injectAnno) {
            // 无注解，递归 深度解析
            injectionDict(currFieldValue, resultList);
            return;
        }
        // 有注解
        if (null != currFieldValue) {
            // targetField 有值不覆盖
            log.debug("inject target field has value, {}.{} value:{}",
                    ClassUtil.getClass(currObj), currField.getName(), currFieldValue);
            return;
        }
        String thisStrategy = InjectUtil.strategy(injectAnno);
        if (StrUtil.isEmpty(thisStrategy)) {
            // thisStrategy为空
            throw new IllegalStateException("inject strategy empty from "
                    + ClassUtil.getClass(currObj) + "." + currField.getName());
        }
        log.debug("inject found inject annotation, obj:{}, field:{}, annotation:{}",
                currObj.getClass().getName(), currField.getName(),
                injectAnno.annotationType().getName());

        // from field
        Object fromFieldValue = InjectQueryUtil.getFromFieldValue(currObj, injectAnno,
                currField.getName());
        if (ToolUtil.isEmpty(fromFieldValue)) {
            log.debug("inject from field null, {}.{}", ClassUtil.getClass(currObj),
                    currField.getName());
            return;
        }
        // 属性被 Inject 或子注解 标注
        // 扁平化值
        List<InjectKey> injectKeys = flatValue(currObj, currField, injectAnno, fromFieldValue);
        if (CollUtil.isNotEmpty(injectKeys)) {
            resultList.addAll(injectKeys);
        }
    }

    private static boolean isExists(List<InjectKey> resultList, Object obj) {
        if (CollUtil.isEmpty(resultList)) {
            return false;
        }
        if (null == obj) {
            // null返回true
            return true;
        }

        return resultList.stream()
                .filter(it -> null != it.getObj())
                .anyMatch(it -> obj.equals(it.getObj()));
    }

    /**
     * 返回fromField的值
     * <pre>
     *     只有fromField有值，才能查询到需要注入的值
     * </pre>
     *
     * @param obj             当前对象
     * @param injectAnno      注入注解
     * @param targetFieldName 当前属性名称，如果注解没有fromFieldName，则根据规则反组装fromFieldName
     * @return fromField有值，没有fromField属性或值为null，返回 null
     * @author chad
     * @since 1 by chad at 2025/5/22
     */
    private static Object getFromFieldValue(Object obj, Annotation injectAnno,
                                            String targetFieldName) {
        Class<?> resultCls = ClassUtil.getClass(obj);
        // check DictItemDto field exists
        String fromFieldName = getFromFieldName(injectAnno, targetFieldName);
        log.debug("{}.{} file item name: {}", resultCls.getName(), targetFieldName, fromFieldName);

        if (!ReflectUtil.hasField(resultCls, fromFieldName)) {
            log.debug("{}.{} has not file item name: {}", resultCls.getName(), targetFieldName, fromFieldName);
            return null;
        }
        try {
            Object fileItemValue = ReflectUtil.getFieldValue(obj, fromFieldName);
            if (null != fileItemValue) {
                log.debug("{}.{} value exists, skip auto injection", resultCls.getName(), fromFieldName);
            }
            return fileItemValue;
        } catch (Exception ex) {
            throw new IllegalStateException(resultCls.getName() + "." + fromFieldName + " get value error", ex);
        }
    }

    private static Object getTargetFieldValue(Object currObj, Field targetField) {
        try {
            return ReflectUtil.getFieldValue(currObj, targetField);
        } catch (Exception ex) {
            throw new IllegalStateException(ClassUtil.getClass(currObj) + "." + targetField.getName() +
                    " get value error", ex);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<InjectKey> flatValue(
            Object currObj, Field targetField, Annotation anno, Object fromFieldValue) {
        // todo 可以给 fromField 也加 spel - chad
        if (ToolUtil.isEmpty(fromFieldValue)) {
            return null;
        }
        HashSet<Object> resultSet;
        if (fromFieldValue instanceof Collection<?>) {
            resultSet = CollUtil.newHashSet((Collection<Object>) fromFieldValue);
        } else if (fromFieldValue instanceof Map<?, ?>) {
            // 取map的value
            resultSet = CollUtil.newHashSet(((Map<?, Object>) fromFieldValue).values());
        } else {
            resultSet = CollUtil.newHashSet(fromFieldValue);
        }

        return Collections.singletonList(new InjectKey<>(anno, currObj, fromFieldValue, targetField,
                resultSet));
    }

    /**
     * 解析 iterable，
     *
     * @param iterable 被解析的{@code iterable}对象
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static void injectionIterable(Iterable<?> iterable, List<InjectKey> resultList) {
        if (CollUtil.isEmpty(iterable)) {
            log.debug("{} empty", iterable.getClass().getName());
            return;
        }

        // 遍历 iterable
        for (Object o : iterable) {
            injectionDict(o, resultList);
        }
    }

    /**
     * 解析Map，仅解析 value
     *
     * @param map 需要被解析的map
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static void injectionMap(Map<?, ?> map, List<InjectKey> resultList) {
        if (CollUtil.isEmpty(map)) {
            log.debug("{} empty", map.getClass().getName());
            return;
        }

        // 遍历 iterable
        injectionIterable(map.values(), resultList);
    }

    /**
     * 解析对象
     *
     * @param currObj 需要解析的对象
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static void injectionObject(Object currObj, List<InjectKey> resultList) {
        Class<?> resultCls = currObj.getClass();
        log.debug("Dict injection: {}", resultCls.getName());

        // fileObj 为基本类型
        if (ClassUtil.isBasicType(resultCls)) {
            log.debug("{} is base type", resultCls.getName());
            return;
        }
        // 循环解析属性
        Field[] fields = ReflectUtil.getFieldsDirectlyHasGetter(resultCls, true);
        if (ArrayUtil.isEmpty(fields)) {
            log.debug("{} has not any field", resultCls.getName());
            return;
        }

        log.debug("{} injection file, field size: {}", resultCls.getName(), fields.length);
        for (Field field : fields) {
            injectionDict(currObj, field, resultList);
        }
    }


    /**
     * 获取字典值注入的 fieldName
     *
     * @param anno          fileId annotation
     * @param currFieldName 字典fieldName
     * @return 注入目标字段的名称，默认为 fileFieldName去年 FileId（如果有该后缀）再拼接上DictItem
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    private static String getFromFieldName(Annotation anno, String currFieldName) {
        String fromField = null;
        if (anno instanceof Inject) {
            // 本身就是 inject
            fromField = ((Inject) anno).fromField();
        } else {
            // 不是 inject，反射获取
            fromField = ReflectUtil.invokeDefaultNull(anno, InjectCst.FROM_FIELD_NAME);
        }
        if (StrUtil.isNotEmpty(fromField)) {
            return fromField.trim();
        }
        // todo 配置, 自动拼接 - chad
        int suffixIndex = currFieldName.indexOf(InjectCst.FIELD_DICT_ITEM_SUFFIX);
        if (suffixIndex > 0) {
            currFieldName = currFieldName.substring(0, suffixIndex);
        }
        return currFieldName + InjectCst.FIELD_DICT_ID_SUFFIX;
    }

    /**
     * 获取属性标识的 Inject 或 Inject子注解
     *
     * @param field 属性
     * @return Inject 或 子注解，否则为null
     * @author chad
     * @since 1 by chad at 2023/9/14
     */
    @Nullable
    public static Annotation getInjectAnnotation(Field field) {
        Annotation[] annoArr = field.getAnnotations();
        if (ArrayUtil.isEmpty(annoArr)) {
            return null;
        }
        for (Annotation anno : annoArr) {
            if (InjectUtil.isInject(anno)) {
                // 注解为inject
                return anno;
            }
            if (InjectUtil.isInjectChild(anno)) {
                // 被 Inject 标注的注解，子注解
                return anno;
            }
        }
        return null;
    }

    private InjectQueryUtil() {
        // do nothing
    }
}
