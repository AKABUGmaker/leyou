package com.leyou.search.dto;

import lombok.Data;

import java.util.Set;

@Data
public class GoodsDTO {
    private Long id; // spuId
    private String subTitle;// 卖点
    private String skus;// sku信息的json结构
//    private Long createTime;
//    private Set<Long> price;
}