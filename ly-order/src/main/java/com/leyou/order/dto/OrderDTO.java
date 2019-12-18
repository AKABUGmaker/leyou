package com.leyou.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private Integer paymentType;
    private Long addressId;
    private List<CartDTO> carts;
}