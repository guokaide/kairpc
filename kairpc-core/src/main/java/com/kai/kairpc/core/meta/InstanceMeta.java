package com.kai.kairpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 描述服务实例的元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceMeta {

    // 核心参数
    private String schema;
    private String host;
    private Integer port;
    private String context;

    // 扩展参数
    private boolean status; // true: online / false: offline
    private Map<String, String> parameters; // 附加的参数，比如加标签：idc: A B C

    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "");
    }
}
