package io.github.chad2li.autoinject.core.util;


import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 反射工具
 *
 * @author chad
 * @date 2022/5/18 23:57
 * @since 1 create by chad
 */
public class ReflectUtil {

    /**
     * 获取有 {@code get, is, has} 方法的属性，并向上层类中查找
     *
     * @param beanClass            查找的类
     * @param withSuperClassFields 是否向上层类查找，true是
     * @return 带有 public 无参{@code get, is, has}方法的属性
     * @date 2022/5/19 00:39
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static Field[] getFieldsDirectlyHasGetter(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        if (null == beanClass) {
            throw new NullPointerException("beanClass cannot be null");
        }
        List<Field> fieldList = new ArrayList<>();

        for (Class searchType = beanClass; searchType != null; searchType = withSuperClassFields ? searchType.getSuperclass() : null) {
            Field[] allFields = searchType.getDeclaredFields();
            if (ArrayUtil.isEmpty(allFields)) {
                continue;
            }
            // 去掉没有 getter 方法的 field
            for (Field f : allFields) {
                String fieldNameFirstUp = f.getName();
                if (fieldNameFirstUp.length() > 1) {
                    fieldNameFirstUp = fieldNameFirstUp.substring(0, 1).toUpperCase() + fieldNameFirstUp.substring(1);
                } else {
                    fieldNameFirstUp = fieldNameFirstUp.toUpperCase();
                }

                String methodName = "get" + fieldNameFirstUp;
                boolean has = null != ReflectUtil.getPublicMethod(searchType, methodName,
                        null);
                if (!has) {
                    methodName = "is" + fieldNameFirstUp;
                    has = null != ReflectUtil.getPublicMethod(searchType, methodName, null);
                }

                if (!has) {
                    methodName = "has" + fieldNameFirstUp;
                    has = null != ReflectUtil.getPublicMethod(searchType, methodName, null);
                }

                if (!has) {
                    // 没有 getter 或 is 获取属性的 public 无参方法
                    continue;
                }
                fieldList.add(f);
            }
        }

        if (CollUtil.isEmpty(fieldList)) {
            return new Field[0];
        }
        return fieldList.toArray(new Field[0]);
    }

    public static Method getPublicMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static String getFieldName(Field field) {
        return null == field ? null : field.getName();
    }

    public static Field[] getFields(Class<?> beanClass) throws SecurityException {
//        return FIELDS_CACHE.computeIfAbsent(beanClass, () -> getFieldsDirectly(beanClass, true));
        return getFieldsDirectly(beanClass, true);
    }

    public static Field[] getFieldsDirectly(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        List<Field> allFields = new ArrayList<>(8);
        Class<?> searchType = beanClass;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (ArrayUtil.isNotEmpty(declaredFields)) {
                allFields.addAll(Arrays.asList(declaredFields));
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }

        return allFields.toArray(new Field[0]);
    }

    public static Field getField(Class<?> beanClass, String name) throws SecurityException {
        final Field[] fields = getFields(beanClass);
        if (ArrayUtil.isEmpty(fields)) {
            return null;
        }
        return Arrays.stream(fields)
                //.parallel()
                .filter((field) -> name.equals(getFieldName(field)))
                .findFirst().orElse(null);
    }

    public static boolean hasField(Class<?> clazz, String fieldName) {
        return null != getField(clazz, fieldName);
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        if (null == obj || StrUtil.isEmpty(fieldName)) {
            return null;
        }
        return getFieldValue(obj, getField(obj instanceof Class ? (Class<?>) obj : obj.getClass(), fieldName));
    }


    /**
     * 获取字段值
     *
     * @param obj   对象，static字段则此字段为null
     * @param field 字段
     * @return 字段值
     */
    public static Object getFieldValue(Object obj, Field field) {
        if (null == field) {
            return null;
        }
        if (obj instanceof Class) {
            // 静态字段获取时对象为null
            obj = null;
        }

        setAccessible(field);
        Object result;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccess for " + field.getDeclaringClass() + "," +
                    field.getName(), e);
        }
        return result;
    }

    public static <T extends AccessibleObject> T setAccessible(T accessibleObject) {
        if (null != accessibleObject && false == accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }

    public static <T> T invokeDefaultNull(Object obj, String method, Object... args) {
        try {
            return ReflectUtil.invoke(obj, method, args);
        } catch (Throwable t) {
            return null;
        }
    }

    public static <T> T invoke(Object obj, Method method, Object... args) throws RuntimeException {
        try {

            return invokeRaw(obj, method, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invoke(Object obj, String methodName, Object... args) {
        Assert.notNull(obj, "Object to get method must be not null!");
        Assert.notNull(methodName, "Method name must be not blank!");

        final Method method = getMethodOfObj(obj, methodName, args);
        if (null == method) {
            throw new IllegalStateException("No such method: [" + methodName + "] from " + obj.getClass());
        }
        return invoke(obj, method, args);
    }

    public static Method getMethodOfObj(Object obj, String methodName, Object... args) throws SecurityException {
        if (null == obj || StrUtil.isEmpty(methodName)) {
            return null;
        }
        return getMethod(obj.getClass(), false, methodName, ClassUtil.getClasses(args));
    }

    public static <T> T invokeRaw(Object obj, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        setAccessible(method);

        // 检查用户传入参数：
        // 1、忽略多余的参数
        // 2、参数不够补齐默认值
        // 3、通过NullWrapperBean传递的参数,会直接赋值null
        // 4、传入参数为null，但是目标参数类型为原始类型，做转换
        // 5、传入参数类型不对应，尝试转换类型
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] actualArgs = new Object[parameterTypes.length];
        if (null != args) {
            for (int i = 0; i < actualArgs.length; i++) {
                actualArgs[i] = args[i];
            }
        }

        return (T) method.invoke(isStatic(method) ? null : obj, actualArgs);
    }

    public static boolean isStatic(Method method) {
        Assert.notNull(method, "Method to provided is null.");
        return Modifier.isStatic(method.getModifiers());
    }

    public static Method getMethod(Class<?> clazz, boolean ignoreCase, String methodName, Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StrUtil.isEmpty(methodName)) {
            return null;
        }

        Method res = null;
        final Method[] methods = getMethods(clazz);
        if (ArrayUtil.isNotEmpty(methods)) {
            for (Method method : methods) {
                if (StrUtil.equals(methodName, method.getName(), ignoreCase)
                        && ClassUtil.isAllAssignableFrom(method.getParameterTypes(), paramTypes)
                        //排除协变桥接方法，pr#1965@Github
                        && (res == null
                        || res.getReturnType().isAssignableFrom(method.getReturnType()))) {
                    res = method;
                }
            }
        }
        return res;
    }

    public static Method[] getMethods(Class<?> beanClass) throws SecurityException {
        Assert.notNull(beanClass, "beanClass cannot be null");
//        return METHODS_CACHE.computeIfAbsent(beanClass,
//                () -> getMethodsDirectly(beanClass, true, true));
        return getMethodsDirectly(beanClass, true, true);
    }

    public static Method[] getMethodsDirectly(Class<?> beanClass, boolean withSupers, boolean withMethodFromObject) throws SecurityException {
        Assert.notNull(beanClass, "beanClass cannot be null");

        if (beanClass.isInterface()) {
            // 对于接口，直接调用Class.getMethods方法获取所有方法，因为接口都是public方法
            return withSupers ? beanClass.getMethods() : beanClass.getDeclaredMethods();
        }
        Set<String> uniqueMethodNameSet = new HashSet<>(8);
        List<Method> allMethods = new ArrayList<>(8);
        Class<?> searchType = beanClass;
        while (searchType != null) {
            if (!withMethodFromObject && Object.class == searchType) {
                break;
            }
            for (Method method : searchType.getDeclaredMethods()) {
                if (uniqueMethodNameSet.add(ReflectUtil.getUniqueKey(method))) {
                    allMethods.add(method);
                }

            }
            searchType = (withSupers && !searchType.isInterface()) ? searchType.getSuperclass() : null;
        }

        return allMethods.toArray(new Method[0]);
    }

    /**
     * 获取方法的唯一键，结构为:
     * <pre>
     *     返回类型#方法名:参数1类型,参数2类型...
     * </pre>
     *
     * @param method 方法
     * @return 方法唯一键
     */
    private static String getUniqueKey(Method method) {
        final StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append('#');
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        Assert.notNull(field, "Field in [" + ClassUtil.getClass(obj) + "] not exist !");

        final Class<?> fieldType = field.getType();
        if (null == value) {
            // 获取null对应默认值，防止原始类型造成空指针问题
            value = ClassUtil.getDefaultValue(fieldType);
        }

        setAccessible(field);
        try {
            field.set(obj instanceof Class ? null : obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccess for " + ClassUtil.getClass(obj) + "." + field.getName(), e);
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Assert.notNull(obj, "obj cannot be null");
        Assert.notNull(fieldName, "fieldName cannot be null");

        final Field field = getField((obj instanceof Class) ? (Class<?>) obj : obj.getClass(), fieldName);
        if (null == field) {
            throw new NullPointerException("Field [" + fieldName + "] is not exist in [" + ClassUtil.getClass(obj) +
                    "]");
        }
        setFieldValue(obj, field, value);
    }

    private ReflectUtil() {
        // do nothing
    }
}
