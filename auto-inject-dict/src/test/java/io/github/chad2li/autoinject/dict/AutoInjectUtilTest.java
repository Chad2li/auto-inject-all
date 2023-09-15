package io.github.chad2li.autoinject.dict;

import cn.hutool.core.util.ReflectUtil;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.properties.DictAutoProperties;
import io.github.chad2li.autoinject.core.util.AutoInjectUtil;
import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import io.github.chad2li.autoinject.dict.dto.DictItemDto;
import io.github.chad2li.autoinject.dict.strategy.DictInjectStrategy;
import io.github.chad2li.autoinject.dict.util.DictInjectUtil;
import lombok.Getter;
import lombok.ToString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DictUtilTest
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/25 15:21
 */
public class AutoInjectUtilTest {
    private DictAutoProperties dictProps;

    private DictInjectStrategy dictInjectStrategy;

    @Before
    public void before() {
        dictProps = new DictAutoProperties();
        dictProps.setDefaultParentId(InjectCst.DEFAULT_PARENT_ID);
        dictProps.setDictIdSuffix(InjectCst.FIELD_DICT_ID_SUFFIX);
        dictProps.setDictItemSuffix(InjectCst.FIELD_DICT_ITEM_SUFFIX);
        dictInjectStrategy = new DictInjectStrategy() {
            @Override
            public List<DictItemDto> list(String... type) {
                return null;
            }

            @Override
            public Map list(List list) {
                return null;
            }
        };
        DictAutoProperties dictProps = new DictAutoProperties();
        ReflectUtil.setFieldValue(dictInjectStrategy, "dictProps", dictProps);
    }

    @Test
    public void queryDictAnnotation() {
        DemoVo demo = demoVo(true);
        // 1.
        Set<InjectKey<InjectDict, Object>> injectKeys = AutoInjectUtil.queryDictAnnotation(demo);
        Set<String> typeSet =
                injectKeys.stream().map(it -> it.getAnno().type()).collect(Collectors.toSet());
        Assert.assertEquals(2, typeSet.size());
        // role没有getter方法
        Assert.assertTrue(typeSet.contains("gender"));
        Assert.assertTrue(typeSet.contains("city"));
        // 2.
        AutoInjectUtil.injectionDict(demo, dictMap(), dictInjectStrategy);
        assertDemo(demo);
        // - list
        assertDemo(demo.getList().get(0));
        assertDemo(demo.getList().get(1));
        assertDemo(demo.getList().get(2));
        for (DemoVo demoI : demo.getSet()) {
            assertDemo(demoI);
        }
        for (DemoVo demoI : demo.getMap().values()) {
            assertDemo(demoI);
        }
    }

    private void assertDemo(DemoVo demo) {
        Assert.assertEquals("男", demo.getGenderItem().getName());
        Assert.assertEquals("浙江", demo.getProvinceDict().getName());
        Assert.assertEquals("杭州", demo.getCityDict().getName());
        Assert.assertNull(demo.getRoleItem());
    }


    private Map<String, DictItemDto<String>> dictMap() {
        Map<String, DictItemDto<String>> dictMap = new HashMap<>();
        // gender
        DictItemDto<String> male = dict("1", "0", "gender", "男");
        DictItemDto<String> female = dict("2", "0", "gender", "女");
        DictItemDto<String> unknown = dict("0", "0", "gender", "未知");
        putDictMap(dictMap, male);
        putDictMap(dictMap, female);
        putDictMap(dictMap, unknown);
        // city
        DictItemDto<String> zhejiang = dict("zhejiang", "0", "city", "浙江");
        DictItemDto<String> anhui = dict("anhui", "0", "city", "安徽");
        DictItemDto<String> hangzhou = dict("hangzhou", "zhejiang", "city", "杭州");
        DictItemDto<String> yiwu = dict("yiwu", "zhejiang", "city", "义乌");
        DictItemDto<String> hefei = dict("hefei", "anhui", "city", "合肥");
        putDictMap(dictMap, zhejiang);
        putDictMap(dictMap, anhui);
        putDictMap(dictMap, hangzhou);
        putDictMap(dictMap, yiwu);
        putDictMap(dictMap, hefei);
        // role
        DictItemDto<String> admin = dict("normal", "0", "role", "管理员");
        DictItemDto<String> normal = dict("admin", "0", "role", "普通用户");
        putDictMap(dictMap, admin);
        putDictMap(dictMap, normal);
        return dictMap;
    }

    private void putDictMap(Map<String, DictItemDto<String>> dictMap, DictItemDto<String> dict) {
        dictMap.put(DictInjectUtil.dictKey(dict), dict);
    }

    private DictItemDto<String> dict(String id, String parentId, String type, String name) {
        return new DictItemDto<>(id, parentId, type, name);
    }


    private DemoVo demoVo(boolean isRoot) {
        DemoVo demoVo = new DemoVo();
        demoVo.setGender("1");
        demoVo.setProvince("zhejiang");
        demoVo.setCity("hangzhou");
        demoVo.role = "Normal";
        if (!isRoot) {
            return demoVo;
        }
        List<DemoVo> list = new ArrayList<>(3);
        demoVo.setList(list);
        for (int i = 0; i < 3; i++) {
            list.add(demoVo(false));
        }
        Map<String, DemoVo> map = new HashMap<>(3);
        demoVo.setMap(map);
        for (int i = 0; i < 3; i++) {
            map.put(String.valueOf(i), demoVo(false));
        }
        Set<DemoVo> set = new HashSet<>(3);
        demoVo.setSet(set);
        for (int i = 0; i < 3; i++) {
            set.add(demoVo(false));
        }

        return demoVo;
    }

    @ToString
    public class DemoVo {
        @Getter
        @InjectDict(type = "gender")
        private String gender;
        @Getter
        @InjectDict(targetField = "provinceDict", type = "city")
        private String province;
        @Getter
        @InjectDict(targetField = "cityDict", type = "city", parentField = "province")
        private String city;
        /**
         * 无get方法属性
         */
        @InjectDict(type = "role")
        private String role;
        @Getter
        private List<DemoVo> list;
        @Getter
        private Map<String, DemoVo> map;
        @Getter
        private Set<DemoVo> set;
        @Getter
        private DictItemDto<String> genderItem;
        @Getter
        private DictItemDto<String> provinceDict;
        @Getter
        private DictItemDto<String> cityDict;
        @Getter
        private DictItemDto<String> roleItem;

        public void setGender(String gender) {
            this.gender = gender;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setList(List<DemoVo> list) {
            this.list = list;
        }

        public void setMap(Map<String, DemoVo> map) {
            this.map = map;
        }

        public void setSet(Set<DemoVo> set) {
            this.set = set;
        }

        public void setGenderItem(DictItemDto<String> genderItem) {
            this.genderItem = genderItem;
        }

        public void setProvinceDict(DictItemDto<String> provinceDict) {
            this.provinceDict = provinceDict;
        }

        public void setCityDict(DictItemDto<String> cityDict) {
            this.cityDict = cityDict;
        }

        public void setRoleItem(DictItemDto<String> roleItem) {
            this.roleItem = roleItem;
        }
    }
}