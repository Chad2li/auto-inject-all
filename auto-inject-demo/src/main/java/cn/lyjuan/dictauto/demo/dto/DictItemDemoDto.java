package cn.lyjuan.dictauto.demo.dto;


import io.github.chad2li.autoinject.dict.dto.DictItemDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author chad
 * @date 2022/5/19 01:05
 * @since 1 create by chad
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DictItemDemoDto extends DictItemDto<Long> {
    private String remark;

    public DictItemDemoDto(Long id, Long parentId, String type, String name, String remark) {
        super(id, parentId, type, name);
        this.remark = remark;
    }
}
