# 发布

1. 滚动发布
2. 蓝绿发布
3. 灰度发布：流量调拨

滚动发布和蓝绿发布，都可以保证大部分节点可用，如果全部停机的话，再一个个节点重启的话，可能会导致：
- 系统停机：无法对外提供服务
- 无法重启：比如说原来有 10 个节点，重启一个节点的时候，所有流量（10倍流量）都会打到这个节点，直接把这个节点打挂了，最终所有节点都无法重启
