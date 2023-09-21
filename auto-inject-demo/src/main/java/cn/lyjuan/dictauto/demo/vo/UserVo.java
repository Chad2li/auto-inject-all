package cn.lyjuan.dictauto.demo.vo;

import cn.lyjuan.dictauto.demo.annotation.InjectPromotionName;
import cn.lyjuan.dictauto.demo.dto.DictItemDemo;
import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chad
 * @date 2022/5/13 22:50
 * @since 1 create by chad
 */
@Data
public class UserVo implements Serializable {
    private int id;
    @InjectDict(type = "GENDER")
    private Long genderId;
    /**
     * 自动解析被注入的属性名，genderId去掉后缀Id，加上后缀Item
     */
    private DictItemDemo genderItem;
    /**
     * 活动id
     */
    @InjectPromotionName(targetField = "promotionName")
    private Long promotionId;
    /**
     * promotionId对应的活动名称
     */
    private String promotionName;


    @InjectDict(type = "LEVEL")
    private Long levelId;
    private DictItemDemo levelItem;
    private String name;
    /**
     * 没有 DictId 后缀
     */
    @InjectDict(type = "AGE")
    private Long age;
    private DictItemDemo ageItem;
    /**
     * 一个字符字段
     */
    @InjectDict(type = "A", targetField = "abDict")
    private Long a;
    private DictItemDemo abDict;
    private AddressVo address;

    private static final long serialVersionUID = 1L;
}
