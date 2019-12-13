package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Payload<T> {
    private String id; //tokenId
    private T info; //载荷中实际存储的内容（UserInfo）
    private Date expiration; //过期时间
}
