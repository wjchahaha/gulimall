package com.jc.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-29 17:44
**/
@Data
@ToString
public class SkuStockVo {
    private Long skuId;
    private boolean hasStock;
}
