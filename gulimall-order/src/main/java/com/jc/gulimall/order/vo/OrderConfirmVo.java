package com.jc.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 18:00
**/
@ToString
@Data
public class OrderConfirmVo {
    //地址信息 `ums_member_receive_address` 在这个表
    @Getter @Setter
    List<MemberAddressVo> address;
    //发票记录

    //优惠券积分等信息
    @Getter @Setter
    Integer integration;
    //订单项
    @Getter @Setter
    List<OrderItemVo> items;

    @Getter @Setter
    String orderToken;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    @Setter
    BigDecimal orderTotalAmount;

    public Integer getCount(){
        int sum = 0;
        if(items != null && items.size() > 0){
            for (OrderItemVo item : items) {
                sum += item.getCount();
            }
        }

        return sum;
    }

    public BigDecimal getOrderTotalAmount(){
        BigDecimal total = new BigDecimal("0");
        if(items != null && items.size() > 0){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    //订单应付金额

    @Setter
    BigDecimal orderPayAmount;
    public BigDecimal getPayPrice(){
        return getOrderTotalAmount();
    }
}
