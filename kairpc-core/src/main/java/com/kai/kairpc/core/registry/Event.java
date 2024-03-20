package com.kai.kairpc.core.registry;

import com.kai.kairpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {
    List<InstanceMeta> data;
}
