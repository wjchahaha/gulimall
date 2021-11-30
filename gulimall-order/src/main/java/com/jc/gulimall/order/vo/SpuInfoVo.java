package com.jc.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 20:38
**/
@Data
@ToString
public class SpuInfoVo {
    private Long id;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 商品描述
     */
    private String spuDescription;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     *
     */
    private BigDecimal weight;
    /**
     * 上架状态[0 - 下架，1 - 上架]
     */
    private Integer publishStatus;
    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
}
