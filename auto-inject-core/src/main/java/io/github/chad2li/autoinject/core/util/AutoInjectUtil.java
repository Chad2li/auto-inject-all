package io.github.chad2li.autoinject.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.chad2li.autoinject.core.annotation.Inject;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.strategy.KeyFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * file id util
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/25 09:05
 */
@Slf4j
public class AutoInjectUtil {

    /**
     * 查询对象中所有FileId注解
     *
     * @param fileObj 对象
     * @return fileId
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static <A extends Annotation, T> Set<InjectKey<A, Object>> queryDictAnnotation(Object fileObj) {
        return injectionDict(null, fileObj, true, null, null);
    }

    /**
     * 注入字典值
     *
     * @param fileObj 对象
     * @param fileMap 字典值map, key:
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    public static void injectionDict(@Nullable String strategy, Object fileObj, Map fileMap,
                                     KeyFunction keyFunction) {
        injectionDict(strategy, fileObj, false, fileMap, keyFunction);
    }

    /**
     * 解析对象，将其中有 {@link Inject} 或有其子注解的属性，自动进行字典值注入
     *
     * @param strategy 处理的策略，查询时为null
     * @param fileObj  对象
     * @param isQuery  是否仅获取注解
     * @param fileMap  所有字典值
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static <A extends Annotation, Key, Value> Set<InjectKey<A, Object>>
    injectionDict(@Nullable String strategy, @Nullable Object fileObj, boolean isQuery,
                  @Nullable Map<Key, Value> fileMap,
                  KeyFunction<A, Value, Object> keyFunction) {
        if (null == fileObj) {
            log.debug("Result is null");
            return Collections.emptySet();
        }
        Set<InjectKey<A, Object>> fileIdSet;
        if (fileObj instanceof Iterable) {
            // iterable
            fileIdSet = injectionIterable(strategy, (Iterable<?>) fileObj, isQuery, fileMap,
                    keyFunction);
        } else if (fileObj instanceof Map) {
            // map
            fileIdSet = injectionMap(strategy, (Map<?, ?>) fileObj, isQuery, fileMap, keyFunction);
        } else {
            // other
            fileIdSet = injectionObject(strategy, fileObj, isQuery, fileMap, keyFunction);
        }
        return CollUtil.newHashSet(fileIdSet);
    }


    /**
     * 解析特定属性
     *
     * @param fileObj 当前对象
     * @param field   对象属性
     * @param isQuery 是否仅获取注解
     * @param fileMap 所有字典值
     * @return 所有FileId注解
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    @Nullable
    private static <A extends Annotation, T, Key, Value> Set<InjectKey<A, Object>>
    injectionDict(@Nullable String strategy, Object fileObj, Field field, boolean isQuery, @Nullable Map<Key, Value> fileMap
            , KeyFunction<A, Value, Object> keyFunction) {
        Class<?> resultCls = fileObj.getClass();
        A injectAnnotation = getInjectAnnotation(field);
        String thisStrategy = AutoInjectUtil.strategy(injectAnnotation);
        if (ObjectUtil.isAllNotEmpty(strategy, thisStrategy) &&
                !CharSequenceUtil.equalsIgnoreCase(strategy, thisStrategy)) {
            // 都不为空且不匹配的注解
            // strategy为空，表示查询
            // thisStrategy为空，需要递归
            return Collections.emptySet();
        }
        // file field value
        Object fieldValue;
        try {
            fieldValue = InjectReflectUtil.getFieldValue(fileObj, field);
        } catch (Exception ex) {
            throw new IllegalStateException(resultCls.getName() + "." + field.getName() + " get value error", ex);
        }
        if (null == injectAnnotation) {
            // 递归 深度解析
            return injectionDict(strategy, fieldValue, isQuery, fileMap, keyFunction);
        }
        log.debug("inject found inject annotation, obj:{}, field:{}, annotation:{}",
                fileObj.getClass().getName(), field.getName(),
                injectAnnotation.annotationType().getName());
        // 属性被 Inject 或子注解 标注
        // 扁平化值
        Set<InjectKey<A, Object>> resultValue = flatValue(injectAnnotation, fieldValue);
        if (isQuery || CollUtil.isEmpty(resultValue)) {
            // 如果仅查询注解，则直接返回
            return resultValue;
        }

        // check DictItemDto field exists
        String fileItemName = getTargetName(injectAnnotation, field.getName());
        log.debug("{}.{} file item name: {}", resultCls.getName(), field.getName(), fileItemName);

        if (!InjectReflectUtil.hasField(resultCls, fileItemName)) {
            log.debug("{}.{} has not file item name: {}", resultCls.getName(), field.getName(), fileItemName);
            return resultValue;
        }
        try {
            Object fileItemValue = InjectReflectUtil.getFieldValue(fileObj, fileItemName);
            if (null != fileItemValue) {
                log.debug("{}.{} value exists, skip auto injection", resultCls.getName(), fileItemName);
                return resultValue;
            }
        } catch (Exception ex) {
            throw new IllegalStateException(resultCls.getName() + "." + fileItemName + " get value error", ex);
        }

        // 设置值
        try {
            setItemValue(fileMap, fileObj, injectAnnotation, fileItemName, fieldValue, keyFunction);
        } catch (Exception e) {
            log.error("auto inject set value error, {}.{} to {}, value:{}",
                    fileObj.getClass().getName(), field.getName(), fileItemName, fieldValue, e);
        }

        return resultValue;
    }

    /**
     * 设置 item 值
     *
     * @param fileMap        值map
     * @param fileObj        设置的属性所在对象
     * @param injectItemName 注入的属性名
     * @param fieldIdValue   注入的属性id值
     * @author chad
     * @since 1 by chad at 2023/9/12
     */
    public static <A extends Annotation> void setItemValue(Map<?, ?> fileMap, Object fileObj, A anno,
                                                           String injectItemName, Object fieldIdValue,
                                                           KeyFunction keyFunction) {
        // 获取值
        Object injectValue = getInjectValue(fileMap, fileObj, anno, fieldIdValue, keyFunction);
        if (null == injectValue) {
            log.info("not found inject value, {}.{}, value:{}", fileObj.getClass().getName(),
                    injectItemName, fieldIdValue);
            return;
        }
        InjectReflectUtil.setFieldValue(fileObj, injectItemName, injectValue);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <A extends Annotation, T> Set<InjectKey<A, Object>> flatValue(A anno, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        HashSet<Object> resultSet;
        if (value instanceof Collection<?>) {
            resultSet = CollUtil.newHashSet((Collection<Object>) value);
        } else if (value instanceof Map<?, ?>) {
            resultSet = CollUtil.newHashSet(((Map<?, Object>) value).values());
        } else {
            resultSet = CollUtil.newHashSet((T) value);
        }

        return Collections.singleton(new InjectKey<>(anno, resultSet));
    }

    /**
     * 解析 iterable，
     *
     * @param iterable 被解析的{@code iterable}对象
     * @param isQuery  是否仅获取注解
     * @param fileMap  所有字典值
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static <A extends Annotation, T, Key, Value> Set<InjectKey<A, Object>>
    injectionIterable(@Nullable String strategy, Iterable<?> iterable, boolean isQuery, @Nullable Map<Key, Value> fileMap,
                      KeyFunction keyFunction) {
        if (CollectionUtil.isEmpty(iterable)) {
            log.debug("{} empty", iterable.getClass().getName());
            return Collections.emptySet();
        }

        Set<InjectKey<A, Object>> fileSet = new HashSet<>(4);
        // 遍历 iterable
        Set<InjectKey<A, Object>> subDictSet;
        for (Object o : iterable) {
            subDictSet = injectionDict(strategy, o, isQuery, fileMap, keyFunction);
            if (CollUtil.isNotEmpty(subDictSet)) {
                fileSet.addAll(subDictSet);
            }
        }
        return fileSet;
    }

    /**
     * 解析Map，仅解析 value
     *
     * @param map     需要被解析的map
     * @param isQuery 是否仅获取注解
     * @param fileMap 所有字典值
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static <A extends Annotation, T, Key, Value> Set<InjectKey<A, Object>>
    injectionMap(@Nullable String strategy, Map<?, ?> map, boolean isQuery, @Nullable Map<Key,
            Value> fileMap,
                 KeyFunction keyFunction) {
        if (CollectionUtil.isEmpty(map)) {
            log.debug("{} empty", map.getClass().getName());
            return Collections.emptySet();
        }

        // 遍历 iterable
        return injectionIterable(strategy, map.values(), isQuery, fileMap, keyFunction);
    }

    /**
     * 解析对象
     *
     * @param fileObj 需要解析的对象
     * @param isQuery 是否仅获取注解
     * @param fileMap 所有字典值
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    private static <A extends Annotation, T, Id, Value> Set<InjectKey<A, Object>> injectionObject(
            @Nullable String strategy, Object fileObj, boolean isQuery,
            @Nullable Map<Id, Value> fileMap, KeyFunction keyFunction) {
        Class<?> resultCls = fileObj.getClass();
        log.debug("Dict injection: {}", resultCls.getName());

        // fileObj 为基本类型
        if (AutoInjectUtil.isBaseType(resultCls)) {
            log.debug("{} is base type", resultCls.getName());
            return Collections.emptySet();
        }
        // 循环解析属性
        Field[] fields = InjectReflectUtil.getFieldsDirectlyHasGetter(resultCls, true);
        if (ArrayUtil.isEmpty(fields)) {
            log.debug("{} has not any field", resultCls.getName());
            return Collections.emptySet();
        }

        log.debug("{} injection file, field size: {}", resultCls.getName(), fields.length);
        Set<InjectKey<A, Object>> fileSet = new HashSet<>(4);
        Set<InjectKey<A, Object>> injectKey;
        for (Field field : fields) {
            injectKey = injectionDict(strategy, fileObj, field, isQuery, fileMap, keyFunction);
            if (null != injectKey) {
                fileSet.addAll(injectKey);
            }
        }
        return fileSet;
    }

    /**
     * 获取字典值注入的 fieldName
     *
     * @param anno     fileId annotation
     * @param fileName 字典fieldName
     * @return 注入目标字段的名称，默认为 fileFieldName去年 FileId（如果有该后缀）再拼接上DictItem
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    private static <A extends Annotation> String getTargetName(A anno, String fileName) {
        Inject inject = anno.annotationType().getAnnotation(Inject.class);
        String targetFieldName = inject.targetFieldName();
        Assert.notEmpty(targetFieldName, "targetFieldName cannot be empty: "
                + anno.annotationType().getName());

        String targetName = ReflectUtil.invoke(anno, targetFieldName);
        if (CharSequenceUtil.isNotEmpty(targetName)) {
            return targetName.trim();
        }

        // todo 配置, 自动拼接
        int suffixIndex = fileName.indexOf(InjectCst.FIELD_DICT_ID_SUFFIX);
        if (suffixIndex > 0) {
            fileName = fileName.substring(0, suffixIndex);
        }
        return fileName + InjectCst.FIELD_DICT_ITEM_SUFFIX;
    }

    /**
     * 获取字典值
     *
     * @param fileMap 文件信息，key: file id
     * @param fileId  file id
     * @param <Id>    id
     * @param <MapK>  如果 fileId是map，表示map的key
     * @param <MapV>  如果 fileId是map，表示map的value
     * @return file dto
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <A extends Annotation, Id, MapV, MapK> Object getInjectValue(
            Map<MapK, MapV> fileMap, Object inObj, A anno, Id fileId,
            KeyFunction<A, Id, MapK> keyFunction) {
        if (ObjectUtil.hasEmpty(fileMap, fileId)) {
            return null;
        }
        MapV subValue;
        MapK key;
        if (fileId instanceof Collection<?>) {
            // 集合
            Collection<Id> idColl = (Collection<Id>) fileId;
            Collection<MapV> resultColl = fileId instanceof Set<?> ? new HashSet<>(idColl.size())
                    : new ArrayList<>(idColl.size());
            for (Id id : idColl) {
                key = keyFunction.key(anno, id, inObj);
                subValue = fileMap.get(key);
                if (ObjectUtil.isNotEmpty(subValue)) {
                    resultColl.add(subValue);
                }
            }
            return resultColl;
        } else if (fileId instanceof Map<?, ?>) {
            // map
            Collection<Id> idColl = ((Map<MapK, Id>) fileId).values();
            Map<MapK, MapV> resultMap = new HashMap<>(idColl.size());
            for (Map.Entry<MapK, Id> entry : ((Map<MapK, Id>) fileId).entrySet()) {
                key = keyFunction.key(anno, entry.getValue(), inObj);
                subValue = fileMap.get(key);
                if (ObjectUtil.isNotEmpty(subValue)) {
                    resultMap.put(entry.getKey(), subValue);
                }
            }
            return resultMap;
        }
        // 简单类型值
        key = keyFunction.key(anno, fileId, inObj);
        return fileMap.get(key);
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
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getInjectAnnotation(Field field) {
        Annotation[] annoArr = field.getAnnotations();
        if (ArrayUtil.isEmpty(annoArr)) {
            return null;
        }
        for (Annotation anno : annoArr) {
            if (anno instanceof Inject) {
                // 注解为inject
                return (A) anno;
            }
            if (null != anno.annotationType().getAnnotation(Inject.class)) {
                // 被 Inject 标注的注解，子注解
                return (A) anno;
            }
        }
        return null;
    }

    /**
     * 判断类是否为基础类型
     *
     * @param cls cls
     * @return true是基础类，不注入字典值
     * @author chad
     * @since 1 by chad at 2023/8/25
     */
    public static boolean isBaseType(@Nullable Class<?> cls) {
        if (null == cls) {
            return true;
        }
        if (cls.isAssignableFrom(Enum.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Integer.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Boolean.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Short.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Byte.class)) {
            return true;
        }
        if (cls.isAssignableFrom(String.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Long.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Double.class)) {
            return true;
        }
        if (cls.isAssignableFrom(Float.class)) {
            return true;
        }
        // Object也为基本类型
        return cls == Object.class;
    }

    /**
     * 获取注解上的策略
     *
     * @param annotation 注解Inject或其子类o
     * @return 策略，如果无则为null
     * @author chad
     * @since 1 by chad at 2023/9/20
     */
    @Nullable
    public static <A extends Annotation> String strategy(@Nullable A annotation) {
        if (null == annotation) {
            return null;
        }
        Inject inject;
        if (isInject(annotation)) {
            inject = (Inject) annotation;
        } else {
            inject = getInjectFromChild(annotation);
        }
        return inject.strategy();
    }

    public static boolean isInject(Annotation annotation) {
        return annotation instanceof Inject;
    }

    public static boolean isInjectChild(Annotation annotation) {
        return null != annotation.annotationType().getAnnotation(Inject.class);
    }

    public static Inject getInjectFromChild(Annotation annotation) {
        return annotation.annotationType().getAnnotation(Inject.class);
    }

    private AutoInjectUtil() {
        // do nothing
    }
}
