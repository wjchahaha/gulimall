package com.jc.gulimall.ware.exception;

import lombok.Data;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 22:03
**/
@Data
public class NoStockException extends RuntimeException {
     Long skuId;
    public NoStockException(Long skuId){
        super("商品-->"+skuId+"无库存");
    }


}
