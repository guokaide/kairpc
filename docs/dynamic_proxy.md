# 动态代理

对比 `class` 和 `interface`，在实例化方面：
* `class` 可以被实例化 (非 `abstract class`)
* `interface` 不可以被实例化（本质上就是 `abstract class`)

一般，实例化 `interface` 的方法是：创建 `interface` 的实现类，然后实例化该实现类，再将该实例向上转型，并且赋值给该接口。

比如说，我们要想实例化 `interface`: `Hello`，需要：
* 创建一个实现类 `HelloImpl`
* 创建 `HelloImpl` 实例
* 将该实例向上转型为 `Hello`，并且赋值给变量 `hello`。

```java
public interface Hello {
    void morning(String name);
}

public class HelloImpl implements Hello {
    @Override
    public void morning(String name) {
        System.out.println("Good morning, " + name);
    }

}

public class Main {
    public static void main(String[] args) {
        Hello hello = new HelloImpl();
        hello.morning("Kai");
    }
}
```

上面这种实例化接口的方式就是 `静态代码` 的方式，这种方式是在编译期就实例化了 `interface`。

那有没有可能不编写实现类，在运行期创建某个 `interface` 的实例呢？

这是可能的。

JDK 提供了一种动态代理（Dynamic Proxy）的机制：可以在运行期动态地创建 `interface` 的实例。

具体步骤如下：
1. 实现 InvocationHandler, 负责接口方法的调用。
2. 通过 `Proxy.newProxyInstance()` 方法创建代理类，这个方法有 3 个参数：
- ClassLoader: 通常使用接口类的 ClassLoader
- 要实现的接口数组：至少要传入一个接口
- 处理接口方法调用的 InvocationHandler 实例。
3. 将 `Proxy.newProxyInstance()` 方法创建的 `Object` 实例强制转型为接口。

比如说：

```java
public class DynamicProxyHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("proxy class: " + proxy.getClass().getName());
        System.out.println("method: " + method);
        if ("morning".equals(method.getName())) {
            System.out.println("Good morning, " + args[0]);
        }
        return null;
    }

}

public class Main {
    public static void main(String[] args) {
        Hello helloProxy = (Hello) Proxy.newProxyInstance(
                Hello.class.getClassLoader(),  // 传入 ClassLoader
                new Class[] {Hello.class},     // 传入要实现的列表
                new DynamicProxyHandler()      // 传入处理调用方法的 InvocationHandler
        );
        helloProxy.morning("Kai");
    }
}
```

动态代理的思想其实很简单，就是在运行期动态地生成动态代理类的字节码并加载到 JVM 中, 其生成的字节码相当于如下静态代码实现。

调用 `helloProxy.morning("Kai")` 的时候，其实就是在调用 `InvocationHandler` 的 `invoke` 方法。

```java
public class HelloDynamicProxy implements Hello {

    InvocationHandler invocationHandler;

    public HelloDynamicProxy(InvocationHandler handler) {
        this.invocationHandler = handler;
    }

    @Override
    public void morning(String name) {
        try {
            invocationHandler.invoke(
                    this,
                    Hello.class.getMethod("morning", String.class),
                    new Object[]{name}
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Hello helloProxy = new HelloDynamicProxy(new DynamicProxyHandler());
        helloProxy.morning("Kai");
    }
}
```

小结：
* JDK 提供了动态代理的功能，允许在运行期动态地创建接口的实例。
* 动态代理通过 Proxy 创建代理对象，然后将接口方法代理给 `InvocationHandler` 执行。
* 核心原理是：在运行期动态生成代理类的字节码，并且加载到 JVM 中。
