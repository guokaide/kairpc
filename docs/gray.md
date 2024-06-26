# gray

## 灰度的设计方案

目前，我们实现的是单个服务的灰度，使用的方式是**流量染色+流量调拨**的方式。

也可以是用户、单个调用的灰度，这个时候，染色就是调用的用户或者当前这次访问。

全链路的灰度：A -> B -> C -> DB, 路径上的所有组件都要染色，比如说，redis、mysql、mq 等中间件和数据库都要灰度。

可以通过 context 参数传递的方式实现，从 Consumer -> Provider，参考：[context_params.md](context_params.md)

根据是否染色，确定是否写入到非正常业务使用的 redis、mysql、mq 等中间件。

全链路的压测：也是通过流量染色实现。

## 灰度的实现方案

灰度路由：通过是否染色以及灰度比例，将流量按比例调拨到灰度节点或者是正常节点。

如果灰度节点比例=正常节点比例，其实就是蓝绿发布。

具体流程：
1. provider: 通过 app.metas 配置节点元数据，判断是否是灰度节点，如果是，则将流量染色，否则不染色。
2. consumer: 通过是否染色以及灰度比例，将流量按比例调拨到灰度节点或者是正常节点。

具体实现如下（[GrayRouter.java](..%2Fkairpc-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkai%2Fkairpc%2Fcore%2Fcluster%2FGrayRouter.java)
）：

假设有 10 个节点，正常节点比例：灰度节点比例 = 9:1

方案一：10 个节点中，节点选择之后，既有正常节点，又有灰度节点，正常节点比例：灰度节点比例 = 9:1
* 优点：每次节点选择的比例都是按照正常节点比例：灰度节点比例 = 9:1。
* 缺点：由于正常节点比例比较高，消费者负载均衡的时候，有可能总是同一类节点，比如说，总是选到正常节点。

方案二：10 个节点中，我们通过概率随机的方式选择，每次选择都可能选择一类节点，节点选择完成之后，最终要么是正常节点，要么是灰度节点。
* 缺点：可能不一定每次节点选择的比例都是按照正常节点比例：灰度节点比例 = 9:1，但是选择的次数越多，越接近这个比例。
* 优点：避免消费者负载均衡的时候，总是同一类节点。

