package com.jc.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-10-16 10:12
**/

/**
 * 将页面可能传过来的所有参数都封装在里边
 */
@Data
public class SearchParam {
    //catalog3Id
    //keyword
    private String keyword;
    private Long catalog3Id;

    //品牌id,价格区间(),attrs,sort,pageNum,hastStock

    private List<Long> brandId;

    private String skuPrice;

    private List<String> attrs;

    private String sort;

    private Integer pageNum;

    private Integer hashStock;


}
