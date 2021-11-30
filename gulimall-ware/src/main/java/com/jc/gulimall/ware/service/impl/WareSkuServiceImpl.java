package com.jc.gulimall.ware.service.impl;

import com.jc.common.utils.R;
import com.jc.gulimall.ware.entity.WareInfoEntity;
import com.jc.gulimall.ware.exception.NoStockException;
import com.jc.gulimall.ware.feign.ProductFeignService;
import com.jc.gulimall.ware.vo.LockStockRes;
import com.jc.gulimall.ware.vo.OrderItemVo;
import com.jc.gulimall.ware.vo.SkuHasStockVo;
import com.jc.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
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
import org.springframework.transaction.annotation.Transactional;


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

    /**
     * 为订单锁库存
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean lockStock(WareSkuLockVo vo) {
        //按照下单地址 找到一个就近仓库 锁定库存

        //找到每个商品在哪个仓库有库存
        String orderSn = vo.getOrderSn();
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareIds(wareIds);
            stock.setNum(item.getCount());
            return stock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareIds();
            if(wareIds == null || wareIds.size() <= 0){
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) {
               Long count =  wareSkuDao.lockSkuStock(skuId,wareId,stock.getNum());
               if (count == 1){//锁成功了
                   skuStocked = true;
                   break;
               }else{//失败了

               }
            }
            //如果有他库存的仓库都不足卖的量了
            if (skuStocked == false){
                throw new NoStockException(skuId);
            }
        }

        return true;
    }
    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}