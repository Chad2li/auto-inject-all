package cn.lyjuan.dictauto.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * promotion
 *
 * @author chad
 * @copyright 2025 chad
 * @since created at 2025/5/22 18:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionVo implements Serializable {

    private Long id;

    private String name;

    private static final long serialVersionUID = 1L;
}
