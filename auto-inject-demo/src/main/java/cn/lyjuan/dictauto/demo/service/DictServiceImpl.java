package cn.lyjuan.dictauto.demo.service;

import cn.lyjuan.dictauto.demo.dto.DictItemDemoDto;
import io.github.chad2li.autoinject.dict.dto.DictItemDto;
import io.github.chad2li.autoinject.dict.strategy.DictInjectStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chad
 * @date 2022/5/19 01:02
 * @since 1 create by chad
 */
@Slf4j
@Service
public class DictServiceImpl extends DictInjectStrategy<Long> {

    @Override
    public List<DictItemDto<Long>> list(String... type) {
        log.info("dict list, type:{}", type);
        List<DictItemDto<Long>> list = new ArrayList<>(10);
        list.add(new DictItemDemoDto(1L, 0L, "CITY", "浙江", "remark11"));
        list.add(new DictItemDemoDto(2L, 0L, "AGE", "2岁", "remark11"));
        list.add(new DictItemDemoDto(3L, 0L, "LEVEL", "三级", "remark11"));
        list.add(new DictItemDemoDto(4L, 0L, "GENDER", "男", "remark11"));
        list.add(new DictItemDemoDto(5L, 0L, "A", "5级", "remark11"));
        return list;
    }
}
