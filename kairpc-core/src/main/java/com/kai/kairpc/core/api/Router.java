package com.kai.kairpc.core.api;

import java.util.List;

/**
 * 路由器：根据规则选择目标服务
 */
public interface Router {

    List<String> choose(List<String> providers);

    Router DEFAULT = providers -> providers;

}
