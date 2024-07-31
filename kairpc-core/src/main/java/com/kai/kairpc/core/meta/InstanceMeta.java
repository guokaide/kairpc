package com.kai.kairpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务实例的元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {

    // 核心参数
    private String schema;
    private String host;
    private Integer port;
    private String context;

    // 扩展参数
    private boolean status; // true: online / false: offline => 表示节点是否可用
    private Map<String, String> parameters = new HashMap<>(); // 附加的参数，比如加标签：idc: A B C

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

    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "kairpc");
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }
}
