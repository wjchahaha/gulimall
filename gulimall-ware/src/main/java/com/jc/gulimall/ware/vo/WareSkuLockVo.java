package com.jc.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 21:39
**/
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> locks;//锁住的信息
}
