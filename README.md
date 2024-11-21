# 待办

# 简介

本项目继承自 `dict-auto`[Gitee](https://gitee.com/CodeinChad/dict-auto)|[Github](https://github.com/Chad2li/dict-auto)

在项目开发过程中，会有一些数据转换的需要，比如：需要将字典code转为对应的字典值；将文件Id转为对应的文件信息；
订单关联的商品id需要转换为商品信息（非通用功能，有独立的业务逻辑，需要自行考量）。    
此时就需要批量查询对应的数据id，再批量查询对应的数据并回填，如下代码：
- UserVo
```java
@Data
public class UserVo implements Serializable {
    ...
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * promotionId对应的活动名称
     */
    private String promotionName;
    ...
}
```
- UserController#list
```java
public List<UserVo> list(){
    // 已有用户信息，需要根据promotionId回填promotionName
    AutoPageHelper.startPage();
    List<UserVo> userList = userMapper.list();
    
    // ******************************* 重复代码 重复代码 重复代码 *******************************
    // 获取所有的promotionId
    Set<Long> promotionIdSet = userList.stream().map(User::getPromotionId).collect(Collectors.toSet());
    // 批量获取promotionName数据, key: promotionId
    Map<Long, String> promotionIdNameMap = promotionMapper.selectNameMapByIds(promotionIdSet);
    
    // 回填promotionName数据
    for(UserVo user : userList){
        String promotionNameMap = promotionIdNameMap.get(user.getPromotionId());
        user.setPromotionName(promotionNameMap);
    }
    // ******************************* 重复代码 重复代码 重复代码 *******************************
        
    return userList;
}    
```

在上述代码中，大量的重复代码，为了让开发关注业务逻辑的功能实现，避免大量重复开发工作，`auto-inject-all`项目应运而生。     
该项目将数值映射工作提取通用方法， 使用注解即可方便实现上述代码中将`genderId`转为`Gender`对象的操作。    

# 三、使用

## 3.1 基础使用    
使用本项目的数值自动映射功能，需要实现三步：    
1. 在方法上使用`@InjectResult`注解，标明当前方法返回值需要执行自动映射功能    
2. 在方法返回值对象的属性上使用自定义（如：`InjectPromotionName`）注解    
3. 实现数值获取策略，提供数值映射的数据    
### 3.1.1 数值映射注解    
参考代码 [UserController#user](auto-inject-demo/src/main/java/cn/lyjuan/dictauto/demo/controller/UserController.java)
```java
public class UserController {
    
    @InjectResult
    @GetMapping("list")
    public BaseRes<List<UserVo>> list() {
        ...
    }
    
}
```
使用`@InjectResult`标明方法的传回值需要进行自动映射    

### 3.1.2 方法返回值注入属性
```java
public class UserVo implements Serializable {
    /**
     * 活动id
     */
    @InjectPromotionName(targetField = "promotionName")
    private Long promotionId;
    /**
     * 活动名称
     */
    private String promotionName;
}
```
`InjectPromotionName`为自定义的注入`promotionName`属性的注解，该注解<font color='green'>关键</font>代码如下：        
```java
@Inject(strategy = "PROMOTION_NAME")
public @interface InjectPromotionName {
    /**
     * 将值注入的属性名 <br/>
     * 由 {@link Inject#targetFieldName()} 值决定
     */
    String targetField() default "";
}
```
- 其上有`@Inject`并标明策略为`PROMOTION_NAME`    
- `targetField`可指定被注入的属性名称。`targetField`名称也可由`Inject#targetFieldName()`自定义    

### 3.1.3 数值获取策略    
需要通过`promotionId`获取到对应`promotionName`的值，为了提高性能并增加自主性，数据获取策略会批量传入对应策略注解
标注的所有值，代码如下：    
```java
@Service
public class PromotionService implements AutoInjectStrategy<Long, Long, String, InjectPromotionName> {
    @Override
    public String strategy() {
        return "PROMOTION_NAME";
    }

    @Override
    public Map<Long, String> list(List<InjectKey<InjectPromotionName, Long>> injectKeys) {
        Map<Long, String> promotionIdNameMap = ... 
        ...
        return promotionIdNameMap;
    }
}
```
所有的策略都需要实现`AutoInjectStrategy`，实现两个方法：    
- strategy:    
需要与`InjectPromotionName`注解上`@Inject#strategy()`值一致，才能找到对应的数据获取策略    
- list:    
批量获取数值映射结果集的方法    

其中四个泛型的含义为：    
- Key:    
第1个泛型，标明`list`方法返回`Map`的`Key`    
- Id:    
第2个泛型，标明`UserVo`里被`@InjectPromotionName`标的属性类型    
- Value:    
第3个泛型，标明`list`方法返回`Map`的`Value`    
- A:  
第4个泛型，标明对应的属性注解`InjectPromotionName`    
## 通用实现    
为了方便开发，本项目实现的基本的注入映射实现，但数据的获取策略还是需要自行实现    
### Normal
如果项目中只有一个属性需要自动映射注入，即可用`Normal`策略    
- [InjectCst#NORMAL_STRATEGY](auto-inject-core/src/main/java/io/github/chad2li/autoinject/core/cst/InjectCst.java#NORMAL_STRATEGY)
策略名为`NORMAL`    
- [InjectNormal](auto-inject-core/src/main/java/io/github/chad2li/autoinject/core/annotation/InjectNormal.java)
属性注入标注    
<font color='yellow'>需要自行实现数据获取功能</font>
### Dict（字典）
- [DictCst#DICT](auto-inject-dict/src/main/java/io/github/chad2li/autoinject/dict/cst/DictCst.java#DICT)
字典策略    
- [InjectDict](auto-inject-dict/src/main/java/io/github/chad2li/autoinject/dict/annotation/InjectDict.java)    
字典属性注解     
- [DictInjectStrategy](auto-inject-dict/src/main/java/io/github/chad2li/autoinject/dict/strategy/DictInjectStrategy.java)
抽象的字典数值获取策略，它提取了通用功能，只保留数据查询功能让开发者自行实现
- [DictItem](auto-inject-dict/src/main/java/io/github/chad2li/autoinject/dict/dto/DictItem.java)
字典信息基类，如果需要返回更多值，也可以自行继承它，它的所有属性都是`protected`    
#### 字典设计思路
这里给大家提供一个思路，将表结构附上，不一定要完全按照这个来设计你自己的字典功能。你应该在理解这个设计思路的基础上创建
你自己的功能    
```sql
CREATE TABLE `sys_dict` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `parent_code` varchar(32) NOT NULL DEFAULT '0' COMMENT '父级code，关联 sys_dict.code，根为0',
  `level` int unsigned NOT NULL DEFAULT '0' COMMENT '层级，根为0',
  `subject` varchar(32) NOT NULL COMMENT '分组，值相同则为一组,关联 DictSubjectEnum',
  `code` varchar(32) NOT NULL COMMENT '字典数值，同一组下唯一，关联 DictCodeEnum',
  `value` varchar(2048) NOT NULL COMMENT '信息，字面值或json',
  `full_code` varchar(1024) NOT NULL COMMENT '层级code以/拼接，根为自身code，一定以/结尾，如：, 1/2/, 1/2/3/',
  `sort` int unsigned NOT NULL DEFAULT '0' COMMENT '降序排序',
  `delete_status` tinyint unsigned NOT NULL DEFAULT '0',
  `create_by` bigint NOT NULL,
  `update_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典或配置表';
```
顺便举一个数据的例子便于理解：   
```sql
INSERT INTO jinbaoduo0.sys_dict 
(parent_code,`level`,subject,code,value,full_code,sort,delete_status,create_by,update_by,create_time,update_time) 
VALUES
	 ('0',0,'WITHDRAW_STATUS','NONE','待申请','WITHDRAW_STATUS/NONE/',5,0,1,1,'2023-09-07 23:50:01','2023-09-07 23:50:01'),
	 ('0',0,'WITHDRAW_STATUS','APPLYING','申请中','WITHDRAW_STATUS/APPLYING/',4,0,1,1,'2023-09-07 23:50:01','2023-09-07 23:50:01'),
	 ('0',0,'WITHDRAW_STATUS','PROCESSING','处理中','WITHDRAW_STATUS/PROCESSING/',3,0,1,1,'2023-09-07 23:50:01','2023-09-07 23:50:01'),
	 ('0',0,'WITHDRAW_STATUS','FINISHED','已完成','WITHDRAW_STATUS/FINISHED/',2,0,1,1,'2023-09-07 23:50:01','2023-09-07 23:50:01'),
	 ('0',0,'WITHDRAW_STATUS','CLOSED','已关闭','WITHDRAW_STATUS/CLOSED/',1,0,1,1,'2023-09-07 23:50:01','2023-09-07 23:50:01');
 ```
### 记录

| 版本    | 创作者  | 时间         | 内容       |
|-------|------|------------|----------|
| 1.0.0 | chad | 2023-09-15 | 自动注入数据   |
|       | chad | 2023-09-21 | 更新README |
| 1.0.3 | chad | 2024-11-11 | 发布all    |
| 1.0.4 | chad | 2024-11-20 | 取消自动启动   |

