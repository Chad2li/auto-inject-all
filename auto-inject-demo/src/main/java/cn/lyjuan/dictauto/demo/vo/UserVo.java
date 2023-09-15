package cn.lyjuan.dictauto.demo.vo;

import cn.lyjuan.dictauto.demo.dto.DictItemDemoDto;
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
    public static final long serialVersionUID = 1L;
    private int id;
    @InjectDict(type = "GENDER")
    private Long genderId;
    /**
     * 不需要编译时自动增加的字典属性
     */
    private DictItemDemoDto genderItem;
    @InjectDict(type = "LEVEL")
    private Long levelId;
    private DictItemDemoDto levelItem;
    private String name;
    /**
     * 没有 DictId 后缀
     */
    @InjectDict(type = "AGE")
    private Long age;
    private DictItemDemoDto ageItem;
    /**
     * 一个字符字段
     */
    @InjectDict(type = "A", targetField = "abDict")
    private Long a;
    private DictItemDemoDto abDict;
    private AddressVo address;

    @InjectDict(type = "ABC")
    private Long promotionStatus;
}
