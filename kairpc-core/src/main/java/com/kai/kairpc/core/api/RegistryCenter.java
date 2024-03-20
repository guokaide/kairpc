package com.kai.kairpc.core.api;

import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.registry.ChangedListener;

import java.util.List;

/**
 * 注册中心
 */
public interface RegistryCenter {

    // provider & consumer
    void start();

    // provider & consumer
    void stop();

    // provider
    void register(String service, InstanceMeta instance);

    void unregister(String service, InstanceMeta instance);

    // consumer
    List<InstanceMeta> fetchAll(String service);

    void subscribe(String service, ChangedListener listener);

    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(String service, InstanceMeta instance) {
        }

        @Override
        public void unregister(String service, InstanceMeta instance) {
        }

        @Override
        public List<InstanceMeta> fetchAll(String service) {
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {
        }
    }
}
