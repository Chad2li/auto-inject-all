package cn.lyjuan.dictauto.demo.vo;

import cn.lyjuan.dictauto.demo.annotation.InjectPromotionName;
import cn.lyjuan.dictauto.demo.dto.DictItemDemo;
import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chad
 * @date 2022/5/13 22:50
 * @since 1 create by chad
 */
@Data
public class UserVo implements Serializable {
    private int id;

    private Long genderId;
    /**
     * 自动解析被注入的属性名，genderId去掉后缀Id，加上后缀Item
     */
    @InjectDict(type = "GENDER")
    private DictItemDemo genderItem;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 使用spel提取指定内容
     */
    @InjectPromotionName(fromField = "promotionId", targetSpel = "value.name")
    private String promotionName;
    /**
     * 整个对象
     */
    @InjectPromotionName
    private PromotionVo promotionItem;
    private List<Long> promotionIdList;
    @InjectPromotionName(fromField = "promotionIdList", targetSpel = "value.name")
    private List<String> promotionNameList;
    private Long levelId;
    @InjectDict(type = "LEVEL")
    private DictItemDemo levelItem;
    private String name;
    /**
     * 没有 DictId 后缀
     */
    private Long age;
    @InjectDict(type = "AGE", fromField = "age")
    private DictItemDemo ageItem;
    /**
     * 一个字符字段
     */
    private Long a;
    @InjectDict(type = "A", fromField = "a")
    private DictItemDemo abDict;
    private AddressVo address;

    private static final long serialVersionUID = 1L;
}
