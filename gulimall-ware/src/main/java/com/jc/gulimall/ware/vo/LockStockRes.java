package com.jc.gulimall.ware.vo;

import lombok.Data;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 21:43
**/
@Data
public class LockStockRes {
    private Long skuId;
    private Integer num;
    private Boolean locked;

}
