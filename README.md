### 待办

### 简介
**本项目继承自[Gitee:dict-auto](https://gitee.com/CodeinChad/dict-auto)、
[Github:](https://github.com/Chad2li/dict-auto)。**   

在项目开发过程中，会有一些数据转换的需要，比如：需要将字典code转为对应的字典值；将文件Id转为对应的文件信息；
订单关联的商品id需要转换为商品信息（非通用功能，有独立的业务逻辑，需要自行考量）。
此时就需要批量查询对应的数据id，再批量查询对应的数据并回填，如下代码：
```java
  public List<User> list(){
    // 已有用户信息，需要将用户性别转为对应的字典值
    AutoPageHelper.startPage();
    List<User> userList = userMapper.list();
    // 获取性别id
    Set<Integer> genderIdSet = userList.stream().map(User::getGenderId).collect(Collectors.toSet());
    // 批量获取性别id数据, key: genderId
    Map<Integer, Gender> genderMap = dictService.mapByTyp(DictConst.Type.GENDER);
    if(CollUtil.isEmpty(genderMap)){
        return userList;
    }
    // 回填性别数据
    for(User user : userList){
        Gender gender = genderMap.get(user.getgGenderId());
        if(null == gender){
            continue;
        }
        user.setGenderItem(gender);
    }
    return userList;
  }    
```
在上述代码中，大量的重复代码，为了让开发关注业务逻辑的功能实现，而避免大量重复开发工作，`auto-inject-all`项目应运而生。 
该项目将数值转换工作提取通用方法，使用注解即可方便实现上述代码中将`genderId`转为`Gender`对象的操作。


### 记录

| 版本    | 创作者  | 时间         | 内容     |
|-------|------|------------|--------|
| 1.0.0 | chad | 2023-09-15 | 自动注入数据 |

