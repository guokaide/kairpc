package com.kai.kairpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kairpc.app")
public class AppConfigProperties {

    private String app = "app1";

    private String namespace = "public";

    private String env = "dev";

}
