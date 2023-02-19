# neko233-toolchain
neko233-toolchain is used to resolve common problem.

and create some utils like use in SpringBoot / Ktor / ...

携带常用工具库 + 补充工具库(命名后缀: 233)

定位: 类似 Guava / Apache Commons. 同时还有 SpringBoot/Ktor Style Utils

同时, 将 SpringBoot 一些不错的机制(validation, AOP, 等), 也实现了一遍～


> Bring elegance back to JVM !

ps: default support to JVM-1.8

# Use
## maven
```xml
<dependency>
   <groupId>com.neko233</groupId>
   <artifactId>neko233-toolchain</artifactId>
   <version>0.6.1</version>
</dependency>
```

## Gradle
```kotlin
implementation("com.neko233:neko233-toolchain:0.6.1")
```

定位: 开箱即用的常用包依赖.
业界常用:
1. 工具类
   1. apache-commons-*
   2. guava
2. 序列化工具
   1. FastJson2

自己额外扩展:
1. action-chain 行为链编程
2. dispatcher-with-delegate 委托方式的分发中心
