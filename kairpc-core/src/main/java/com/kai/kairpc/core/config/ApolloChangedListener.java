package com.kai.kairpc.core.config;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@Data
public class ApolloChangedListener implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @ApolloConfigChangeListener({"app.yml"})
    public void onChange(ConfigChangeEvent event) {
        for (String key : event.changedKeys()) {
            ConfigChange change = event.getChange(key);
            log.info("Found change: {} -> {}", key, change);
        }

        // 更新 Bean 的属性，主要是 @@ConfigurationProperties 标记的 Bean
        // org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder.onApplicationEvent 处理此事件
        applicationContext.publishEvent(new EnvironmentChangeEvent(event.changedKeys()));
    }
}
