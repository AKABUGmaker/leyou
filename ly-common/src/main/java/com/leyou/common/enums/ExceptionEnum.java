package com.leyou.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {


    //定义一个枚举常量值，PRICE_NOT_BE_NULL，"构造参数"
    PRICE_CANNOT_BE_NULL(400, "价格不能为空！"),
    CATEGORY_NOT_FOUND(204,"对应的分类未找到"),
    DATA_TRANSFER_ERROR(500,"查询到的数据与数据库数据转换异常"),
    BRAND_NOT_FOUND(204,"查询的品牌结果为空");

    private int status;
    private String message;

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}