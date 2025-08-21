# 一、简介

本项目继承自 `dict-auto`[Gitee](https://gitee.com/CodeinChad/dict-auto)|[Github](https://github.com/Chad2li/dict-auto)

在项目开发过程中，会有一些数据转换的需要，比如：需要将字典code转为对应的字典值；
将文件Id转为对应的文件信息； 订单关联的商品id需要转换为商品信息等。    
通常的做法是使用关联表查询、或者先查询主数据再填充其他信息，然而这些方式通用性不强
且有代码侵入性。    
此项目应运而生。
该项目通过反射解析到需要注入的属性，并根据匹配的策略获取对应的数据，再将数据填充到
属性中。    

如下代码，`promotionName`是通过`promotionId`查询活动数据，并将活动名称自动填充
到`promotionName`字段：
```java
public class UserVo implements Serializable {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动名称
     */
    @Inject(strategy = "PROMOTION", fromField = "promotionId")
    private String promotionName;
}
```

# 二、简单使用

使用本项目的数值自动注入功能，需要实现三步：
1. 在方法上使用`@InjectResult`注解，标明当前方法的返回值需要执行自动注入功能；
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

2. 在返回值对象的属性上使用注入注解（`Inject`），标明`strategy（策略）`、
   `fromField（来源）`和`targetSpel（取值的spel表达式）`；

```java
public class UserVo implements Serializable {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动名称
     */
    @Inject(strategy = "PROMOTION", fromField = "promotionId", targetSpel = "value.name")
    private String promotionName;
}
```
- `strategy` 标明策略为`PROMOTION`；
- `fromField` 标明需要`promotionId`来查询`promotionName`；
- `targetSpel` 标明取策略返回对象的`name`属性为值，`value`为el表达式的根元素。

3. 实现策略，提供自动注入的数据    
需要通过`promotionId`获取到对应`promotionName`的值，代码如下：

```java

@Service
public class PromotionService implements AutoInjectStrategy<Long, Long, String, InjectPromotionName> {
    @Override
    public String strategy() {
        return "PROMOTION_NAME";
    }

    // ids 是根据Inject注解的fromField采集到的所有promotionId值
    // 返回的key为promotionId
    @Override
    public Map<Long, PromotionDto> list(Set<Long> ids) {
        //根据 promotionIdSet 查询promotion，key: promotionId
        Map<Long, PromotionDto> promotionIdMap = ... 
        ...
        return promotionIdMap;
    }
}
// promotion对象
public class PromotionDto {
    private Long id;
    // targetSpel = "value.name"：取此值自动注入
    private String name;
}
```

上述3步即可在全局实现根据`promotionId`自动注入`promotionName`。

# 三、更多功能
## 3.1 自定义注解
## 3.2 自定义key
## 3.3 额外操作
## 3.4 targetSpel

# 四、字典


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

通常情况下，我们只需要根据id查询，实现`userFillQuery`方法，并返回`false`；并实现`list(Set<Id> idSet)`
方法，即可实现简单的id查询。    
为避免大数据量导致查询失败，需要分批查询，防止一次查询过多数据。

## 通用实现

为了方便开发，本项目实现的基本的注入映射实现，但数据的获取策略还是需要自行实现

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

### 版本记录
- 1.0.0 chad 2023-09-15     
  自动注入数据    
  更新README
- 1.0.3 chad 2024-11-11    
  发布all
- 1.0.4 chad 2024-11-20    
  取消自动启动
- 1.0.6 chad 2024-11-21    
  兼容hutool版本
- 2.0.0 chad 2025-05-21
  去除hutool依赖     
  使用SPEL注入字段    
  解析结果映射对象，填充时不再解析

### 后续计划
- 支持缓存解析结果，相同对象不重复解析，支持动态对象

