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


        //分类id和品牌id拼接上
        wrapper.eq("catalog_id", params.get("catelogId")).eq("brand_id", params.get("brandId"));

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO 高级部分完善优惠券，满减信息保存失败后 事务回滚等功能
     *
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1保存spu的基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        //设置创建时间和修改时间
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        //1
        this.save(spuInfoEntity);

        //2保存spu的描述图片 `pms_spu_info_desc`  List<String> decript;
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        List<String> decript = vo.getDecript();

        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescService.save(spuInfoDescEntity);

        //3保存spu的图片集  `pms_spu_images`    List<String> images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);
        //4保存spu的规格参数（一些属性) pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveBaseAttrs(spuInfoEntity.getId(), baseAttrs);
        //保存spu的积分信息  gulimall->`sms_spu_bounds`
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        Bounds bounds = vo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());

        R r1 = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r1.getCode() != 0) {
            log.error("远程spu积分失败");
        }
        //保存当前spu的所有sku信息  `pms_sku_info`
        List<Skus> skus = vo.getSkus();
        //5.1)保存sku的基本信息 `pms_sku_info`
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
                //保存skuInfoEntity
                skuInfoService.save(entity);
                Long skuId = entity.getSkuId();
                //5.2）保存sku的图片列表 `pms_sku_images`   private List<Images> images;'
                List<Images> images1 = sku.getImages();
                //收集SkuImagesEntity
                List<SkuImagesEntity> collect = images1.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(image, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).filter((item) -> {
                    //true的话不过滤  false过滤掉
                    return !StringUtils.isEmpty(item.getImgUrl());
                }).collect(Collectors.toList());
                //if imgurl 为null 则不保存sku对应的图片信息
////                skuImagesService.saveBatch(collect);
//                for(SkuImagesEntity skuImagesEntity : collect){
//                    if (skuImagesEntity.getImgUrl() != null)
//                    skuImagesService.save(skuImagesEntity);
//                }
                //5.3）保存sku的销售属性值 `pms_sku_sale_attr_value` List<Attr> attr;
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> collect1 = attr.stream().map((item) -> {
                    SkuSaleAttrValueEntity saleAttr = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(item, saleAttr);
                    saleAttr.setSkuId(skuId);

                    return saleAttr;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(collect1);

                //5.4）保存sku的优惠及满减信息；gulimall-sms->`sms_sku_ladder(打折表几件打几折)`
                //`sms_sku_full_reduction`(满减信息)`sms_member_price`（会员价格)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                //满的件数必须大于0 || 减的钱数必须大于0
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r = couponFeignService.saveSkuReductionTo(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
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
        //要上架的商品 存储咋es中的模型
        List<SkuEsModel> upProducts = new ArrayList<>();
        //远程查询商品是否有库存

        //查询spu对应的所有sku的信息
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);


        //远程调用查看储存信息  skuid,hasStock
        //先过去skuids
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
            log.error("库存服务异常,原因{}", e);
        }

        //查询当前的sku的所有可能可以用来被检索的属性
        // 将spu关联的属性都查出来
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrBySpuId(spuId);
        //过滤id
        List<Long> attrids = attrValueEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        //collect1中有重复的attrId,进行去重
        List<Long> searchAttrs=attrService.selectSearchAttrsIds(attrids);
        Set<Long> idSet = new HashSet<>(searchAttrs);

        List<SkuEsModel.Attrs> attrsList = attrValueEntities.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        //查询分类信息
        CategoryEntity categoryEntity = categoryService.getById(skuInfoEntities.get(0).getCatalogId());
        //查询品牌信息
        BrandEntity brandEntity = brandService.getById(skuInfoEntities.get(0).getBrandId());
        Map<Long, Boolean> finalSkuHasStock = skuHasStock;
        List<SkuEsModel> collect = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            //sku基本信息进行对拷
            BeanUtils.copyProperties(sku, skuEsModel);
            //设置skuPrice 和skuImg brandImg  brand_name catalogName Attrs
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            skuEsModel.setCatalogName(categoryEntity.getName());
            //设置atts属性
            skuEsModel.setAttrs(attrsList);

            // 设置 hasStock 远程调用去库存系统中查
            if (finalSkuHasStock == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalSkuHasStock.get(sku.getSkuId()));
            }

            // hotScore  默认设置0
            skuEsModel.setHotScore(0L);

            return skuEsModel;
        }).collect(Collectors.toList());

        //数据发送给es进行保存
        //1.先在es中写好接口

        //2.远程调用
        R r = searchFeignService.productUp(collect);
        if (r.getCode() == 0){
            //远程调用成功

            //修改当前spu状态
            this.baseMapper.updateSpuStatus(spuId, SpuStatus.SPU_UP.getCode());
        }else{
            //远程调用失败
            //TODO 重复调用问题 ？ 接口幂等性； 重试机制
            /**
             * Feign调用流程
             * 1.构造请求数据  将对象转为json
             *
             * 2.发送请求并执行请求（执行成功会解码响应数据）
             *
             * 3.执行请求会有重试机制
             *
             * while(true)
             * try{
             *
             * }catch(Exception e){
             *  重试
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