package com.jc.gulimall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.ware.dao.PurchaseDetailDao;
import com.jc.gulimall.ware.entity.PurchaseDetailEntity;
import com.jc.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        /**
         *  wrapper.eq("purchase_id",key)
         *                     .or().eq("sku_id",key)
         *                     .or().like("sku_num",key)
         *                     .or().eq("sku_price",key);
         * where purchase_id = key
         * or sku_id = key
         * or sku_name like key
         * or sku_price = key
         * and ...
         * and ...
         */
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("purchase_id", key)
                        .or().eq("sku_id", key)
                        .or().eq("sku_price", key);
            });
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}