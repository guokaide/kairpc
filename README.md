# kairpc

> 基于 SpringBoot 实现的 RPC 框架。

## 基本用法

### 引入依赖

引入 SpringBoot 依赖：

```java
<parent>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-parent</artifactId>
<version>3.2.3</version>
<relativePath/><!--lookup parent from repository-->
</parent>

<dependencies>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
</dependency>
</dependencies>
```

引入 RPC 依赖：

```xml

<dependency>
    <groupId>com.kai</groupId>
    <artifactId>kairpc-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 服务提供者

通过 `@KaiProvider` 注解提供服务。

要想提供用户服务：

```java
public interface UserService {
    User findById(int id);
}
```

第一步：服务提供者在该接口的实现类上标记 `@KaiProvider` 注解，这里的实现类必须是一个 Bean:

```java
import com.kai.kairpc.core.annotation.KaiProvider;
import org.springframework.stereotype.Service;

@Service
@KaiProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "Kai-" + System.currentTimeMillis());
    }
}
```

第二步：通过 `@Import({ProviderConfig.class})` 将 RPC 服务提供者启动类注册为 Bean，处理服务消费者请求。

```java

@SpringBootApplication
@Import({ProviderConfig.class})
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

第三步：对外提供服务

```java
import org.springframework.web.bind.annotation.RestController;
import com.kai.kairpc.core.provider.ProviderBootstrap;

@RestController
public class ProviderController {

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }

}
```

### 服务消费者

通过 `@KaiConsumer` 将服务引入到 Bean 中。

第一步：要想引入用户服务，可以在 Bean 的属性上标记 `@KaiConsumer`, 即可像调用本地方法一样，调用用户服务。

```java
import com.kai.kairpc.core.annotation.KaiConsumer;
import org.springframework.stereotype.Component;

public class UserAppService {

    @KaiConsumer
    UserService userService;

    public void test() {
        User user = userService.findById(100);
        System.out.println("test ===> " + user);
    }
}
```

第二步：通过 `@Import(ConsumerConfig.class)` 将 RPC 服务消费者启动类注册为 Bean，提交请求给服务提供者。

```java

@SpringBootApplication
@Import(ConsumerConfig.class)
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}
```

## 实现原理

### 服务提供者

服务提供者的职责是：接收 RPCRequest，交给对应的服务去处理，返回 RPCResponse。

服务提供者通过 `@KaiProvider` 注解将 Bean 注册为一个服务，对外提供服务。

服务提供者的核心实现类是 [ProviderBootstrap.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fprovider%2FProviderBootstrap.java)。

这个类在初始化（init()）的时候，会将所有标记 `@KaiProvider` 注解的 Bean, 全部都缓存到 skeleton 中。

具体实现如下：

1. 获取对外暴露的服务：获取所有标记 `@KaiProvider` 注解的 Bean，这些 Bean 就是服务的实现类
    - 由于 `ProviderBootstrap` 实现了 `ApplicationContextAware` 接口，因此，可以获取到 `ApplicationContext`。
    - 通过 `ApplicationContext` 可以获取所有标记 `@KaiProvider` 注解的实现类
2. 本地注册服务：将这些对外暴露的服务的信息全部缓存到 skeleton 中
    - 获取这些服务实现类的接口，通过反射获取接口的方法、方法签名
    - 将服务的信息保存在 skeleton 中，包括服务名、实现类、方法、方法签名
3. 处理 RPC 请求：通过 `ProviderBootstrap.invoke()` 方法处理 RPCRequest
    - 根据服务名在 skeleton 中获取服务
    - 根据方法签名获取服务的具体的处理方法，接口可能存在重载的方法，所以这里要用方法签名
    - 预处理参数，将参数转换为正确的类型
    - 执行该方法，获取结果
    - 通过 RpcResponse 包装结果返回给服务消费者
4. 通过 `ProviderConfig` 创建并初始化 `ProviderBootstrap`

这里将服务提供方提供的实现类的方法签名全部都缓存起来，原因是：

1. 服务提供方提供的方法是有限的，即使全部缓存起来也没有问题
2. 如果没有缓存，服务消费方每次调用，都需要计算方法签名的话，会影响性能

### 服务消费者

服务消费者的核心职责是：将服务的调用包装成 RPCRequest 提交给服务提供者，收到 RPCResponse 之后，提取需要的数据。

服务消费者的核心实现类是 [ConsumerBootstrap.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fconsumer%2FConsumerBootstrap.java)。

这个类在初始化的时候（调用 start()）的时候，会将所有 Bean 中标记 `@KaiConsumer` 的属性全部创建出来，缓存到 stub 中。

具体实现如下：

1. 获取 `@KaiConsumer` 标记的属性：获取所有 Bean 中标记 `@KaiConsumer` 的属性
    - 由于 `ConsumerBootstrap` 实现了 `ApplicationContextAware` 接口，因此，可以获取到 `ApplicationContext`。
    - 通过 `ApplicationContext` 获取所有的 Bean
    - 获取所有 Bean 中标记 `@KaiConsumer` 的属性
2. 初始化 `@KaiConsumer` 标记的属性

- 创建这些属性的代理类，并且赋值给这些属性
- 将这些代理类缓存在 stub 中

3. `ConsumerConfig` 类创建 `ConsumerBootstrap`, 并通过 `ApplicationRunner` 的方式初始化 `ConsumerBootstrap`

这里的代理类，是通过 JDK 的动态代理实现的，这里的代理类主要是负责发起 RPC 请求，提交到服务提供者，并获取响应。

要注意的是，获取所有 Bean 中标记 `@KaiConsumer` 的属性的时候，这些 Bean 很有可能是 Spring
做了字节码增强的代理类，所以，我们可能还需要通过其父类才能获取到所有的属性。

### Router、LoadBalancer、Filter

Router
主要职责是：根据规则选择目标服务，其接口定义：[Router.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fapi%2FRouter.java)

LoadBalancer
主要职责是：根据规则选择提供服务的实例，其接口定义为：[LoadBalancer.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fapi%2FLoadBalancer.java)

- [RandomLoadBalancer.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fcluster%2FRandomLoadBalancer.java)
- [RoundRobinBalancer.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fcluster%2FRoundRobinBalancer.java)

Filter
主要职责是：做一些前置或者后置处理，其接口定义为：[Filter.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fapi%2FFilter.java)

服务消费者通过 Router 和 LoadBalancer 获取最终的请求地址。

### 注册中心

注册中心的职责是：实现服务的注册和发现。其定义为：[RegistryCenter.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fapi%2FRegistryCenter.java)

这里选择 ZooKeeper
实现注册中心，实现类为：[ZkRegistryCenter.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fregistry%2FZkRegistryCenter.java)

注册的路径结构如下：

```
- kairpc 
   - com.kai.kairpc.demo.api.UserService (服务名，持久化节点)
      - 192.168.1.102_8081 (实例地址1，临时节点)
      - 192.168.1.102_8082 (实例地址2, 临时节点）
   - com.kai.kairpc.demo.api.OrderService
      - 192.168.1.102_8081
      - 192.168.1.102_8082
```

在 [ProviderConfig.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fprovider%2FProviderConfig.java)
和 [ConsumerConfig.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fconsumer%2FConsumerConfig.java)
可以配置选择什么注册中心。

[ProviderBootstrap.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fprovider%2FProviderBootstrap.java) 
在启动的时候，通过 start() 方法将实例注册到注册中心，在停止的时候，通过 stop() 方法将实例从注册中心移除。

[ConsumerBootstrap.java](kairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fconsumer%2FConsumerBootstrap.java)
在启动的时候，通过 createFromRegistry() 方法获取注册中心服务提供者的实例，并且通过监听注册中心的事件动态调整可以获取的服务提供者的实例。
