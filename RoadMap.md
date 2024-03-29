

# RoadMap


## version

### 1.1.2
1. [Add] 

### v1.1.1
1. [BugFix] KvTemplate Bug

### v1.1.0
1. [Add] 在 JDK 8 基础下, 实现了 JDK 11+ 的 List.of / Map.of 效果. 见 MapUtils233, CollectionUtils
2. [Add] MathTextCalculator233 追加模板计算, 实现超复杂的动态上下文数据提供的科学数学运算~
3. [Add] KvTemplate 追加反向解析, 优化性能~
4. [Add] 行数据库 RowDataBase, 列数据库 ColumnDatabase, 逆向索引 InvertedIndex, 多版本并发控制 MvccMemoryDatabase
5. [Add] AopProxy233 动态代理
6. [Add] Raft API
7. [Add] 2PC API 

### v1.0.0
1. [Add] Terminal 终端操作
2. [Refactor] 调整包名
3. [Add] Kv API

-----------

### v0.6.2
1. [Add] BitUtils233 bit 工具.
2. [Optimize] FunctionText233 优化复杂情况

### v0.6.1
1. [Add] [Compress] 压缩工具, Huffman
2. [Add] [Compress] 压缩日期, punchDate
3. [Add] [Encrypt] Rsa / Salt 
4. [Add] [File] BigFileSplitUtils233
5. [Rename] [API] dao -> dao_api
6. [Format] [Constant] CharPool..

### v0.6.0
deploy v0.6.0
1. [Add] cache/async 异步 DAO 的读写方案.
2. [Add] Actor 的 Java 模型, 相比传统 Actor, 提供了 Actor LoadBalance 机制, 避免了 thread hungry problem
3. [Add] AOP 机制. 相比传统 AOP, 集成了 with retry

### v0.5.0
deploy v0.5.0
1. [Add] VcsMessage 版本号消息 & 差异应用
2. [Add] Validator233 . 完善校验器
3. [Add] add data struct 一些数据结构
4. [Delete] 删除 jakarta-validation-api 包, 不纳入默认实现, 采用 neko233 自己的 @annotation

### v0.4.1
1. [Add] TimeCostUtils233 时间计时工具
2. [Add] Abstract WAL 抽象WAL 
3. [Add] StringUtils233 追加常用方法, 处理 StringText -> StringObject 等
4. [Add] MathTextCalculator233 通过{文本}实现的复杂数学公式计算.
5. [Add] FileUtils233 追加一些文件处理方法
6. [Add] 压缩工具 Zip

### v0.4.0
1. [Rename] 包重命名, 项目更名为 neko233-toolchain
2. [Add] WAL @Experimental 
3. [Add] FileUtils233, ObjectUtils233 相关方法
4. [BugFix] Regex 的转义
5. [Add] BloomFilter 

### v0.1.7
deploy v0.1.7
1. [Update] [idGenerator] add 'cache' API
2. [Add] [Validate] add my validate demo & test unit.
3. [Add] [Event] add dispatcher helper.


### v0.1.6
deploy 0.1.6
1. [Add] ReactiveData 响应式, 基础
2. [Update] [事件机制] 破坏性改动. destroy history API. DispatcherCenter.java 删除 --> EventDispatcher.java
3. [Add] [Test] 事件委托 & 响应式的单元测试.

### v0.1.5
1. [Add] Validator 注解,参数校验器. 和 spring-boot-starter-validation 差不多

data
```java
public class ValidateDto {
    @Digits.List({@Digits(integer = 1, fraction = 0)})
    private long userId;
    private String name;
    private Integer age;
    @Email(regexp = ".*@qq.com")
    private String email;
}
```

validate
```java
        Validator.scanPackage(JakartaEmailValidator.class.getPackage().getName());
        ValidateDto build = ValidateDto.builder()
                .email("123@gmail.com")
                .build();
        ValidateContext validate = Validator.validate(build);
        Assert.assertFalse(validate.isOk());
```


### v0.1.4
1. [Add] IdGenerator | RDS, SnowFlake

```java

```