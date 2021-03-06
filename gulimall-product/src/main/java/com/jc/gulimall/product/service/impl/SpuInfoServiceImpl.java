package com.jc.gulimall.product.service.impl;


import com.alibaba.fastjson.TypeReference;
import com.jc.common.constant.SpuStatus;
import com.jc.common.to.SkuEsModel;
import com.jc.common.to.SkuHasStockVo;
import com.jc.common.to.SkuReductionTo;
import com.jc.common.to.SpuBoundsTo;
import com.jc.common.utils.R;
import com.jc.gulimall.product.dao.SpuInfoDescDao;
import com.jc.gulimall.product.entity.*;
import com.jc.gulimall.product.feign.CouponFeignService;
import com.jc.gulimall.product.feign.SearchFeignService;
import com.jc.gulimall.product.feign.WareFeignService;
import com.jc.gulimall.product.service.*;
import com.jc.gulimall.product.vo.*;
import jdk.internal.org.objectweb.asm.Handle;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.jws.Oneway;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("spu_name", key).or().eq("spu_description", key);
        }


        //??????id?????????id?????????
        wrapper.eq("catalog_id", params.get("catelogId")).eq("brand_id", params.get("brandId"));

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO ????????????????????????????????????????????????????????? ?????????????????????
     *
     * @param vo
     */

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1??????spu??????????????? pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        //?????????????????????????????????
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        //1
        this.save(spuInfoEntity);

        //2??????spu??????????????? `pms_spu_info_desc`  List<String> decript;
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        List<String> decript = vo.getDecript();

        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescService.save(spuInfoDescEntity);

        //3??????spu????????????  `pms_spu_images`    List<String> images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);
        //4??????spu??????????????????????????????) pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveBaseAttrs(spuInfoEntity.getId(), baseAttrs);
        //??????spu???????????????  gulimall->`sms_spu_bounds`
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        Bounds bounds = vo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());

        R r1 = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r1.getCode() != 0) {
            log.error("??????spu????????????");
        }
        //????????????spu?????????sku??????  `pms_sku_info`
        List<Skus> skus = vo.getSkus();
        //5.1)??????sku??????????????? `pms_sku_info`
        if (skus != null && skus.size() > 0) {
            skus.forEach(sku -> {
                String defaultUrl = "";
                for (Images img : sku.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultUrl = img.getImgUrl();
                    }
                }
                SkuInfoEntity entity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, entity);
                entity.setSpuId(spuInfoEntity.getId());
                entity.setBrandId(vo.getBrandId());
                entity.setCatalogId(vo.getCatalogId());
                entity.setSaleCount(0L);
                entity.setSkuDefaultImg(defaultUrl);
                //??????skuInfoEntity
                skuInfoService.save(entity);
                Long skuId = entity.getSkuId();
                //5.2?????????sku??????????????? `pms_sku_images`   private List<Images> images;'
                List<Images> images1 = sku.getImages();
                //??????SkuImagesEntity
                List<SkuImagesEntity> collect = images1.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(image, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).filter((item) -> {
                    //true???????????????  false?????????
                    return !StringUtils.isEmpty(item.getImgUrl());
                }).collect(Collectors.toList());
                //if imgurl ???null ????????????sku?????????????????????
////                skuImagesService.saveBatch(collect);
//                for(SkuImagesEntity skuImagesEntity : collect){
//                    if (skuImagesEntity.getImgUrl() != null)
//                    skuImagesService.save(skuImagesEntity);
//                }
                //5.3?????????sku?????????????????? `pms_sku_sale_attr_value` List<Attr> attr;
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> collect1 = attr.stream().map((item) -> {
                    SkuSaleAttrValueEntity saleAttr = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(item, saleAttr);
                    saleAttr.setSkuId(skuId);

                    return saleAttr;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(collect1);

                //5.4?????????sku???????????????????????????gulimall-sms->`sms_sku_ladder(????????????????????????)`
                //`sms_sku_full_reduction`(????????????)`sms_member_price`???????????????)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                //????????????????????????0 || ????????????????????????0
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r = couponFeignService.saveSkuReductionTo(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("????????????sku??????????????????");
                    }
                }


            });

        }


    }


    @Override
    public PageUtils queryPageByCondation(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                // ( A or B)
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void spuUp(Long spuId) {
        //?????????????????? ?????????es????????????
        List<SkuEsModel> upProducts = new ArrayList<>();
        //?????????????????????????????????

        //??????spu???????????????sku?????????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);


        //??????????????????????????????  skuid,hasStock
        //?????????skuids
        List<Long> skuIds = skuInfoEntities.stream().map(skuInfoEntity -> {
            return skuInfoEntity.getSkuId();
        }).collect(Collectors.toList());
        Map<Long, Boolean> skuHasStock = null;
        try {
            R r = wareFeignService.hasStockBySkuIds(skuIds);

            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};

            List<SkuHasStockVo> data = r.getData(typeReference);

            skuHasStock = data.stream().collect(Collectors.toMap(vo -> {
                return vo.getSkuId();
            }, item -> {
                return item.isHasStock();
            }));
        } catch (Exception e) {
            log.error("??????????????????,??????{}", e);
        }

        //???????????????sku?????????????????????????????????????????????
        // ???spu???????????????????????????
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrBySpuId(spuId);
        //??????id
        List<Long> attrids = attrValueEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        //collect1???????????????attrId,????????????
        List<Long> searchAttrs=attrService.selectSearchAttrsIds(attrids);
        Set<Long> idSet = new HashSet<>(searchAttrs);

        List<SkuEsModel.Attrs> attrsList = attrValueEntities.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        //??????????????????
        CategoryEntity categoryEntity = categoryService.getById(skuInfoEntities.get(0).getCatalogId());
        //??????????????????
        BrandEntity brandEntity = brandService.getById(skuInfoEntities.get(0).getBrandId());
        Map<Long, Boolean> finalSkuHasStock = skuHasStock;
        List<SkuEsModel> collect = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            //sku????????????????????????
            BeanUtils.copyProperties(sku, skuEsModel);
            //??????skuPrice ???skuImg brandImg  brand_name catalogName Attrs
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            skuEsModel.setCatalogName(categoryEntity.getName());
            //??????atts??????
            skuEsModel.setAttrs(attrsList);

            // ?????? hasStock ?????????????????????????????????
            if (finalSkuHasStock == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalSkuHasStock.get(sku.getSkuId()));
            }

            // hotScore  ????????????0
            skuEsModel.setHotScore(0L);

            return skuEsModel;
        }).collect(Collectors.toList());

        //???????????????es????????????
        //1.??????es???????????????

        //2.????????????
        R r = searchFeignService.productUp(collect);
        if (r.getCode() == 0){
            //??????????????????

            //????????????spu??????
            this.baseMapper.updateSpuStatus(spuId, SpuStatus.SPU_UP.getCode());
        }else{
            //??????????????????
            //TODO ?????????????????? ??? ?????????????????? ????????????
            /**
             * Feign????????????
             * 1.??????????????????  ???????????????json
             *
             * 2.??????????????????????????????????????????????????????????????????
             *
             * 3.??????????????????????????????
             *
             * while(true)
             * try{
             *
             * }catch(Exception e){
             *  ??????
             * }
             */
        }


    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        SpuInfoEntity byId1 = this.getById(byId.getSpuId());

        return byId1;
    }

}