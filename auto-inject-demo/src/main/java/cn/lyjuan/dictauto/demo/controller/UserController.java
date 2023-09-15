package cn.lyjuan.dictauto.demo.controller;

import cn.lyjuan.dictauto.demo.vo.AddressVo;
import cn.lyjuan.dictauto.demo.vo.UserVo;
import io.github.chad2li.autoinject.core.annotation.InjectResult;
import io.github.chad2li.baseutil.http.vo.res.BaseRes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自动注入样式
 *
 * @author chad
 * @date 2022/5/18 19:49
 * @since 1 create by chad
 */
@RestController
@RequestMapping
public class UserController {

    @InjectResult
    @GetMapping("user/{id}")
    public BaseRes<UserVo> user(@PathVariable Long id) {
        return BaseRes.succ(user());
    }

    @InjectResult
    @GetMapping("map")
    public BaseRes<Map> map() {
        Map<String, UserVo> map = new HashMap<>();
        map.put("user", user());

        return BaseRes.succ(map);
    }

    @InjectResult
    @GetMapping("list")
    public BaseRes<List> list() {
        List<UserVo> list = new ArrayList();
        list.add(user());
        Set<Long> genderSet = list.stream().map(UserVo::getGenderId).collect(Collectors.toSet());
        return BaseRes.succ(list);
    }


    private UserVo user() {
        UserVo user = new UserVo();
        user.setId(1);
        user.setName("ZhangSan");
        user.setAge(2L);
        user.setLevelId(3L);
        user.setGenderId(4L);
        user.setA(5L);
        AddressVo address = new AddressVo();
        address.setCityDictId(1L);
        user.setAddress(address);
        return user;
    }
}
