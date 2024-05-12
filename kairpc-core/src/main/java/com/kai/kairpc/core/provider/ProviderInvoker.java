package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.api.RpcException;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.governance.SlidingTimeWindow;
import com.kai.kairpc.core.meta.ProviderMeta;
import com.kai.kairpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ProviderInvoker {

    private final MultiValueMap<String, ProviderMeta> skeleton;
    private final Map<String, SlidingTimeWindow> serviceSlidingTimeWindows = new HashMap<>();
    // TODO: 对服务实例做全局限流
    private final Map<String, String> trafficControls;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
        this.trafficControls = providerBootstrap.getProviderConfigProperties().getTrafficControls();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        processContextParameters(request);

        String service = request.getService();
        int trafficControl = Integer.parseInt(trafficControls.getOrDefault(service, "20"));
        synchronized (serviceSlidingTimeWindows) {
            SlidingTimeWindow window = serviceSlidingTimeWindows.computeIfAbsent(service, k -> new SlidingTimeWindow());
            int invoked = window.calcSum();
            if (invoked > trafficControl) {
                throw new RpcException("Service " + service + " invoked [" + invoked + "] times in 30s, larger than tpsLimit: "
                        + trafficControl, RpcException.EXCEED_LIMIT_EX);
            }
            window.record(System.currentTimeMillis());
            log.debug("Service {} in window with {}", service, window.getSum());
        }

        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        List<ProviderMeta> providerMetas = skeleton.get(service);
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RpcException(e.getCause().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RpcException(e.getMessage()));
        } finally {
            clearContextParameters();
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] actualArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArgs[i] = TypeUtils.cast(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actualArgs;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }

    private void processContextParameters(RpcRequest request) {
        RpcContext.getContextParameters().putAll(request.getParams());
    }

    private void clearContextParameters() {
        // 清理上下文，防止内存泄露和上下文污染
        RpcContext.clearContextParameters();
    }

}
