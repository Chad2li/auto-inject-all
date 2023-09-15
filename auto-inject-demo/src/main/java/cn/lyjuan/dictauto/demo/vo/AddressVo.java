package cn.lyjuan.dictauto.demo.vo;

import io.github.chad2li.autoinject.dict.annotation.InjectDict;
import lombok.Data;

/**
 * 地址
 *
 * @author chad
 * @since 1 create by chad at 2022/8/25 14:39
 */
@Data
public class AddressVo {
    @InjectDict(type = "CITY")
    private Long cityDictId;
}
