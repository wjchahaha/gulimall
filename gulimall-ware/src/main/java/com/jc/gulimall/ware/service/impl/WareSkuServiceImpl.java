package com.jc.gulimall.ware.service.impl;

import com.jc.common.utils.R;
import com.jc.gulimall.ware.entity.WareInfoEntity;
import com.jc.gulimall.ware.feign.ProductFeignService;
import com.jc.gulimall.ware.vo.SkuHasStockVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.ware.dao.WareSkuDao;
import com.jc.gulimall.ware.entity.WareSkuEntity;
import com.jc.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String wareId = (String) params.get("wareId");

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();


        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        String sku_id = (String) params.get("sku_id");

        if (!StringUtils.isEmpty(sku_id)) {
            wrapper.eq("sku_id", sku_id);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params), wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId).eq("ware_id", wareId);
        WareSkuEntity one = this.getOne(wrapper);

        if (one == null) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //远程调用
            //TODO 还可以用什么办法出现异常后不回滚？
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            //插入
            wareSkuDao.insert(wareSkuEntity);
        } else {
            //修改
            wareSkuDao.addStack(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //循环查表
            Long stock=baseMapper.getSkuWare(skuId);
            vo.setSkuId(skuId);
            //如果sku库存表中没有的话  则stoack为这里会出现空指针异常
            vo.setHasStock(stock == null? false:stock > 0);

            return vo;
        }).collect(Collectors.toList());


        return collect;
    }

}