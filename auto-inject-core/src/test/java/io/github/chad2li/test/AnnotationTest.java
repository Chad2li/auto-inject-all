package io.github.chad2li.test;

import cn.hutool.core.util.ReflectUtil;
import io.github.chad2li.autoinject.core.annotation.NormalInject;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * AnnotationTest
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/14 12:30
 */
public class AnnotationTest {
    @Test
    public void reflectMethod() {
        Field genderField = ReflectUtil.getField(Demo.class, "gender");
        Annotation anno = genderField.getAnnotation(NormalInject.class);
        String targetName = ReflectUtil.invoke(anno, "targetField");
        Assert.assertEquals("abc", targetName);
    }

    public static class Demo {
        @NormalInject(targetField = "abc")
        private Integer gender;
    }
}
