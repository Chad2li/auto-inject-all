package io.github.chad2li.autoinject.dict;

import io.github.chad2li.autoinject.core.annotation.Inject;
import io.github.chad2li.autoinject.core.cst.InjectCst;
import io.github.chad2li.autoinject.core.dto.InjectKey;
import io.github.chad2li.autoinject.core.properties.DictAutoProperties;
import io.github.chad2li.autoinject.core.strategy.AutoInjectStrategy;
import io.github.chad2li.autoinject.core.util.InjectQueryUtil;
import io.github.chad2li.autoinject.core.util.InjectSetUtil;
import io.github.chad2li.autoinject.core.util.ReflectUtil;
import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import io.github.chad2li.autoinject.dict.dto.DictItem;
import io.github.chad2li.autoinject.dict.strategy.DictInjectStrategy;
import io.github.chad2li.autoinject.dict.util.DictInjectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    private AutoInjectStrategy schoolStrategy;

    private static final String SCHOOL_STRATEGY_NAME = "SCHOOL";

    @Before
    public void before() {
        dictProps = new DictAutoProperties();
        dictProps.setDefaultParentId(InjectCst.DEFAULT_PARENT_ID);
        dictProps.setDictIdSuffix(InjectCst.FIELD_DICT_ID_SUFFIX);
        dictProps.setDictItemSuffix(InjectCst.FIELD_DICT_ITEM_SUFFIX);
        dictInjectStrategy = new DictInjectStrategy() {
            @Override
            public List<DictItem> list(String... type) {
                return null;
            }

            @Override
            public Map list(List list) {
                return null;
            }
        };
        schoolStrategy = new AutoInjectStrategy<Long, Long, SchoolDemo, Inject>() {
            @Override
            public String strategy() {
                return SCHOOL_STRATEGY_NAME;
            }

            @Override
            public boolean useFullQuery() {
                return false;
            }

            @Override
            public Map<Long, SchoolDemo> list(Set<Long> longs) {
                Map<Long, SchoolDemo> map = new HashMap<>(1);
                map.put(11L, new SchoolDemo(11L, "测试学校-11"));
                return map;
            }
        };
        DictAutoProperties dictProps = new DictAutoProperties();
        ReflectUtil.setFieldValue(dictInjectStrategy, "dictProps", dictProps);
    }

    @Test
    public void queryDictAnnotation() {
        DemoVo demo = demoVo(true);
        // 1.
        List<InjectKey> injectKeys = InjectQueryUtil.queryDictAnnotation(demo);
        Set<String> typeSet =
                injectKeys.stream()
                        .map(InjectKey::getAnno)
                        .filter(it -> it instanceof InjectDict)
                        .map(it -> ((InjectDict) it).type()).collect(Collectors.toSet());
        Assert.assertEquals(2, typeSet.size());
        // role没有getter方法
        Assert.assertTrue(typeSet.contains("gender"));
        Assert.assertTrue(typeSet.contains("city"));
        // 2.
        InjectSetUtil.setInjectValue(injectKeys, dictMap(), dictInjectStrategy);
        InjectSetUtil.setInjectValue(injectKeys, schoolStrategy.list(injectKeys), schoolStrategy);
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
        Assert.assertEquals("测试学校-11", demo.schoolName);
        Assert.assertNull(demo.getRoleItem());
    }


    private Map dictMap() {
        Map<String, DictItem<String>> dictMap = new HashMap<>();
        // gender
        DictItem<String> male = dict("1", "0", "gender", "男");
        DictItem<String> female = dict("2", "0", "gender", "女");
        DictItem<String> unknown = dict("0", "0", "gender", "未知");
        putDictMap(dictMap, male);
        putDictMap(dictMap, female);
        putDictMap(dictMap, unknown);
        // city
        DictItem<String> zhejiang = dict("zhejiang", "0", "city", "浙江");
        DictItem<String> anhui = dict("anhui", "0", "city", "安徽");
        DictItem<String> hangzhou = dict("hangzhou", "zhejiang", "city", "杭州");
        DictItem<String> yiwu = dict("yiwu", "zhejiang", "city", "义乌");
        DictItem<String> hefei = dict("hefei", "anhui", "city", "合肥");
        putDictMap(dictMap, zhejiang);
        putDictMap(dictMap, anhui);
        putDictMap(dictMap, hangzhou);
        putDictMap(dictMap, yiwu);
        putDictMap(dictMap, hefei);
        // role
        DictItem<String> admin = dict("normal", "0", "role", "管理员");
        DictItem<String> normal = dict("admin", "0", "role", "普通用户");
        putDictMap(dictMap, admin);
        putDictMap(dictMap, normal);
        return dictMap;
    }

    private void putDictMap(Map<String, DictItem<String>> dictMap, DictItem<String> dict) {
        dictMap.put(DictInjectUtil.dictKey(dict), dict);
    }

    private DictItem<String> dict(String id, String parentId, String type, String name) {
        return new DictItem<>(id, parentId, type, name);
    }


    private DemoVo demoVo(boolean isRoot) {
        DemoVo demoVo = new DemoVo();
        demoVo.setGenderId("1");
        demoVo.setProvince("zhejiang");
        demoVo.setCity("hangzhou");
        demoVo.role = "Normal";
        demoVo.schoolId = 11L;
        demoVo.self = demoVo;
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

    @ToString(exclude = {"self"})
    public class DemoVo {
        @Getter
        private String genderId;
        @Getter
        @InjectDict(type = "gender")
        private DictItem<String> genderItem;
        @Getter
        private String province;
        @Getter
        private String city;
        /**
         * 无get方法属性
         */
        @InjectDict(type = "role")
        private String role;
        private Long schoolId;
        @Getter
        @Inject(strategy = SCHOOL_STRATEGY_NAME, fromField = "schoolId", targetSpel = "value.name")
        private String schoolName;
        @Getter
        private List<DemoVo> list;
        @Getter
        private Map<String, DemoVo> map;
        @Getter
        private Set<DemoVo> set;
        @Getter
        @InjectDict(fromField = "province", type = "city")
        private DictItem<String> provinceDict;
        @Getter
        @InjectDict(fromField = "city", type = "city", parentField = "province")
        private DictItem<String> cityDict;
        @Getter
        private DictItem<String> roleItem;
        // 测试循环
        @Getter
        private DemoVo self;

        public void setGenderId(String genderId) {
            this.genderId = genderId;
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

        public void setGenderItem(DictItem<String> genderItem) {
            this.genderItem = genderItem;
        }

        public void setProvinceDict(DictItem<String> provinceDict) {
            this.provinceDict = provinceDict;
        }

        public void setCityDict(DictItem<String> cityDict) {
            this.cityDict = cityDict;
        }

        public void setRoleItem(DictItem<String> roleItem) {
            this.roleItem = roleItem;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SchoolDemo {
        private Long id;
        private String name;
    }
}