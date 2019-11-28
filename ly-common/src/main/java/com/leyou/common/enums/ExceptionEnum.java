package com.leyou.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {


    //定义一个枚举常量值，PRICE_NOT_BE_NULL，"构造参数"
    PRICE_CANNOT_BE_NULL(400, "价格不能为空！");

    private int status;
    private String message;

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}