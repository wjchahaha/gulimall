package com.jc.gulimall.product.vo;

import com.jc.gulimall.product.entity.SkuImagesEntity;
import com.jc.gulimall.product.entity.SkuInfoEntity;
import com.jc.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.jc.gulimall.product.entity.SpuInfoDescEntity;
import com.sun.org.apache.bcel.internal.generic.LSTORE;
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
* @create: 2021-11-05 20:08
**/
@ToString
@Data
public class SkuItemVo {

    //1.sku基本信息获取
    SkuInfoEntity info;
    //2.sku图片信息
    List<SkuImagesEntity> images;
    //3.sku的销售属性
    List<SkuItemSaleAttrVo> saleAttr;
    //4.spu介绍
    SpuInfoDescEntity desc;
    //5.规则参数信息
    List<SpuItemAttrGroupAttrVo> groupAttrs;


}
