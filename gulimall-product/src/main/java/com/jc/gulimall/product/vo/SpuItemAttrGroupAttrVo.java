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
public class SpuItemAttrGroupAttrVo {
    private String groupName;
    private List<Attr> attrs;
}
