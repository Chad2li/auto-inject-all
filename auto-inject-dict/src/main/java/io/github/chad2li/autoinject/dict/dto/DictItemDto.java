package io.github.chad2li.autoinject.dict.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典内容
 *
 * @author chad
 * @since 1 create by chad at 2022/5/18 00:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictItemDto<I> implements Serializable {
    /**
     * dict id，同一类型一唯一
     */
    protected I id;
    /**
     * 父级
     */
    protected I parentId;
    /**
     * 字典类型
     */
    protected String type;
    /**
     * 字典值
     */
    protected String name;

    private static final long serialVersionUID = 1L;
}
