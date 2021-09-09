package com.jc.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.SkuInfoDao;
import com.jc.gulimall.product.entity.SkuInfoEntity;
import com.jc.gulimall.product.service.SkuInfoService;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)){
           wrapper.eq("brand_id",brandId);
        }
        String min = (String) params.get("min");
        String max = (String) params.get("max");

//        if (!StringUtils.isEmpty(min) && !StringUtils.isEmpty(max)){
//            wrapper.between("price",min,max);
//        }
        //bug更改
        if (!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }
        if (!StringUtils.isEmpty(max)){
            BigDecimal bigDecimal = new BigDecimal(max);
            //如果超过0才<max
            if (bigDecimal.compareTo(new BigDecimal("0")) == 1){
                wrapper.le("price",max);
            }
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);

    }

}