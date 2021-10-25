package com.jc.gulimall.search.vo;

import com.jc.common.to.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-10-16 10:50
**/
@Data
public class SearchRespVo {
    /**
     * 分页信息
     */
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    //所有的商品模型
    private List<SkuEsModel> products;

    //所有品牌
    private List<BrandVo> brands;

    //分类
    private List<CatalogVo> catalogs;

    //所有的共有属性
    private List<AttrVo> attrs;

    //品牌vo
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        //对应brand中的logo
        private String brandImg;
    }
    //分类vo
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;

    }

    //属性vo
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}


