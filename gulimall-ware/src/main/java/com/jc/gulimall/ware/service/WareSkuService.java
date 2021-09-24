package com.jc.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.ware.entity.WareSkuEntity;
import com.jc.gulimall.ware.vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:34:53
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> hasStock(List<Long> skuIds);
}

