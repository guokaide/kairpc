# 跨服务参数传递

## 需求讨论

现在有一个需求，我们希望在跨服务调用时，可以在不同服务之间传递参数，比如 traceId。

调用路径如下：消费者(traceId) -> 网关(traceId) -> 服务A(traceId) -> 服务B(traceId) -> 消费者(traceId)。

我们希望整个调用链中，都能获取到 traceId，并且将该traceId传递到下一个服务中。

## 方案设计

### 方案一：通过HTTP Header传递

现在，我们的 RPC 框架是基于 HTTP 协议的，因此，我们可以通过 HTTP Header 来传递参数，但是这种方案是和 HTTP 协议绑定的，因此我们希望有一种不依赖 HTTP 协议的方案。

### 方案二：通过 RPC Context 传递参数

我们通过 RPC Context 存储传递的参数。

- 消费者端：通过 Filter 将参数存储到 RPC Request 中，通过 RPC Request 将参数传递到提供者端。
- 提供者端：从 RPC Request 中获取参数。

但是，要注意的是:

- 由于目前请求的处理是通过线程池完成的，而 RPC Context 要存储的参数是线程绑定的，因此，RPC Context 存储参数的时候要线程隔离，比如使用 ThreadLocal。
- 由于 RPC Context 是线程绑定的，因此：
  - RPC Context 中的参数在跨线程调用的时候，可能会丢失。因此，我们需要在跨线程调用的时候，将 RPC Context 中的参数传递到下一个线程中。
  - 当一个请求结束时，RPC Context 中与该请求相关的所有参数都应该清理。
- 传递的参数如果比较大，对性能会造成影响：
  - 影响序列化和反序列化的性能。
  - 影响网络传输的性能。