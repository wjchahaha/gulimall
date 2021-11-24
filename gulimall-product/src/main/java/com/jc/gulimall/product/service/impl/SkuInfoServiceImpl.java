package com.jc.gulimall.product.service.impl;

import com.jc.gulimall.product.entity.SkuImagesEntity;
import com.jc.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.jc.gulimall.product.entity.SpuInfoDescEntity;
import com.jc.gulimall.product.service.*;
import com.jc.gulimall.product.vo.SkuItemSaleAttrVo;
import com.jc.gulimall.product.vo.SkuItemVo;
import com.jc.gulimall.product.vo.SpuItemAttrGroupAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.SkuInfoDao;
import com.jc.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondation(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        String max = (String) params.get("max");

//        if (!StringUtils.isEmpty(min) && !StringUtils.isEmpty(max)){
//            wrapper.between("price",min,max);
//        }
        //bug更改
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }
        if (!StringUtils.isEmpty(max)) {
            BigDecimal bigDecimal = new BigDecimal(max);
            //如果超过0才<max
            if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                wrapper.le("price", max);
            }
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);

    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skus = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        return skus;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo vo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.sku基本信息获取
            SkuInfoEntity skuInfoEntity = getById(skuId);
//            Long catalogId = skuInfoEntity.getCatalogId();
            vo.setInfo(skuInfoEntity);

            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3.spu的销售属性
            List<SkuItemSaleAttrVo> skuSaleAttrValueEntities =
                    skuSaleAttrValueService.getSkuSaleAttrBySpuId(res.getSpuId());
            vo.setSaleAttr(skuSaleAttrValueEntities);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4.spu介绍
            SpuInfoDescEntity spuDesc = spuInfoDescService.getById(res.getSpuId());
            vo.setDesc(spuDesc);
        }, executor);

        CompletableFuture<Void> groupAttrsFuture = infoFuture.thenAcceptAsync((res) -> {
            //5.规则参数信息
            List<SpuItemAttrGroupAttrVo> groupAttrs = attrGroupService.getAttrGroupBySpuId(res.getSpuId(), res.getCatalogId());
            vo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //2.sku图片信息
            List<SkuImagesEntity> images = skuImagesService.getImagesById(skuId);
            vo.setImages(images);
        }, executor);

        CompletableFuture.allOf(saleAttrFuture,descFuture,groupAttrsFuture,imagesFuture).get();


        return vo;
    }

}