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
* @create: 2021-09-11 01:01
**/
@Data
public class MegroVo {
    /**
     * {
     *   purchaseId: 1, //采购单id
     *   items:[1,2,3,4] //合并项集合
     * }
     */
    private Long purchaseId;

    private List<Long> items;
}
