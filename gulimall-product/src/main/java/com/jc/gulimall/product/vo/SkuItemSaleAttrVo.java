package com.jc.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-08 20:06
**/
@Data
@ToString
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private String attrValues;
}
