package com.kai.kairpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "kairpc.provider")
public class ProviderProperties {

    // for provider

    private Map<String, String> metas = new HashMap<>();

    private Map<String, String> trafficControls = new HashMap<>();

}
