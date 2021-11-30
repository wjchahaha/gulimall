package com.jc.gulimall.order.to;

import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.entity.OrderItemEntity;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 13:32
**/
@Data
@ToString
public class OrderCreateTo {
    private OrderEntity orderEntity;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
}
