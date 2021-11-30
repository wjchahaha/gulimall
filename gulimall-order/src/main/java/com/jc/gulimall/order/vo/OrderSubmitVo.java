package com.jc.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 12:12
**/
@ToString
@Data
public class OrderSubmitVo {
    private Long addrId;//收获地址id
    private Integer payType;//支付方式

    private String orderToken;//防重令牌
    private BigDecimal payPrice;//应付价格
    //用户相关的信息直接从session中取

    private String note;//订单备注
}
