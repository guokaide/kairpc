package com.kai.kairpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kairpc.app")
public class AppProperties {

    private String app = "app1";

    private String namespace = "public";

    private String env = "dev";

}
