package com.guyuexuan.bjxd.model;

import java.io.Serializable;

// Record 自动拥有只读属性、全参构造、toString、equals、hashCode
public record QinglongEnv(
        long id,
        String value,
        String timestamp,
        int status,
        long position,
        String name,
        String remarks,
        int isPinned,
        String createdAt,
        String updatedAt
) implements Serializable {
}
