package com.jc.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.to.mq.StockLockedTo;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.ware.entity.WareSkuEntity;
import com.jc.gulimall.ware.vo.LockStockRes;
import com.jc.gulimall.ware.vo.SkuHasStockVo;
import com.jc.gulimall.ware.vo.WareSkuLockVo;

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

    Boolean lockStock(WareSkuLockVo vo);

    void unlock(StockLockedTo to);
}

