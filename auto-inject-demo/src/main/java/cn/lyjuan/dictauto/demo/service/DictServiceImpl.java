package cn.lyjuan.dictauto.demo.service;

import cn.lyjuan.dictauto.demo.dto.DictItemDemo;
import io.github.chad2li.autoinject.dict.dto.DictItem;
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
    public List<DictItem<Long>> list(String... type) {
        log.info("dict list, type:{}", type);
        List<DictItem<Long>> list = new ArrayList<>(10);
        list.add(new DictItemDemo(1L, 0L, "CITY", "浙江", "remark11"));
        list.add(new DictItemDemo(2L, 0L, "AGE", "2岁", "remark11"));
        list.add(new DictItemDemo(3L, 0L, "LEVEL", "三级", "remark11"));
        list.add(new DictItemDemo(4L, 0L, "GENDER", "男", "remark11"));
        list.add(new DictItemDemo(5L, 0L, "A", "5级", "remark11"));
        return list;
    }
}
