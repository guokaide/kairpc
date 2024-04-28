package com.kai.kairpc.core.cluster;

import com.kai.kairpc.core.api.Router;
import com.kai.kairpc.core.meta.InstanceMeta;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由（0-100）
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    // grayRatio 表示灰度节点流量占比
    @Setter
    private int grayRatio = -1;

    private static final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    // 实现方案有 2 种：
    // 1. grayRatio = 10, normal: gray = 1:9
    // => normal 10% 的节点，gray 90% 的节点
    // 但是这种方案要求 LoadBalancer 必须是线性均匀的，否则可能会总选到一类节点
    // 2. 通过随机数把节点划分为 2 类
    // A 的情况下，全部返回 normal
    // B 的情况下，全部返回 gray
    // 这种方案就与 LoadBalancer 无关了
    // 方法 2 非常的精妙
    @Override
    public List<InstanceMeta> choose(List<InstanceMeta> providers) {
        if (grayRatio == -1) {
            return providers;
        }

        if (providers == null || providers.size() < 1) {
            return providers;
        }

        List<InstanceMeta> normalProviders = new ArrayList<>();
        List<InstanceMeta> grayProviders = new ArrayList<>();
        providers.forEach(p -> {
            String gray = p.getParameters().get("gray");
            if ("true".equals(gray)) {
                grayProviders.add(p);
            } else {
                normalProviders.add(p);
            }
        });

        log.debug(" grayRouter normalNodes/grayNodes, grayRatio ===> {}/{},{}",
                normalProviders.size(), grayProviders.size(), grayRatio);

        if (grayRatio <= 0) {
            return normalProviders;
        }
        if (grayRatio >= 100) {
            return grayProviders;
        }

        if (random.nextInt(100) < grayRatio) {
            log.debug(" grayRouter grayNodes ===> {}", grayProviders);
            return grayProviders;
        } else {
            log.debug(" grayRouter normalNodes ===> {}", normalProviders);
            return normalProviders;
        }
    }
}
