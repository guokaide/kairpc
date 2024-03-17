package com.kai.kairpc.core.api;

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
    void register(String service, String instance);

    void unregister(String service, String instance);

    // consumer
    List<String> fetchAll(String service);

    void subscribe(String service, ChangedListener listener);

    class StaticRegistryCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(String service, String instance) {
        }

        @Override
        public void unregister(String service, String instance) {
        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {
        }
    }
}
