package com.jc.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.jc.common.constant.OrderConstant;
import com.jc.common.constant.OrderStatusEnum;
import com.jc.common.to.mq.StockLockedTo;
import com.jc.common.utils.R;
import com.jc.common.vo.MemberEntity;
import com.jc.gulimall.ware.entity.WareInfoEntity;
import com.jc.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.jc.gulimall.ware.entity.WareOrderTaskEntity;
import com.jc.gulimall.ware.exception.NoStockException;
import com.jc.gulimall.ware.feign.OrderFeignService;
import com.jc.gulimall.ware.feign.ProductFeignService;
import com.jc.gulimall.ware.service.WareOrderTaskDetailService;
import com.jc.gulimall.ware.service.WareOrderTaskService;
import com.jc.gulimall.ware.vo.*;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;
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
            Long stock = baseMapper.getSkuWare(skuId);
            vo.setSkuId(skuId);
            //如果sku库存表中没有的话  则stoack为这里会出现空指针异常
            vo.setHasStock(stock == null ? false : stock > 0);

            return vo;
        }).collect(Collectors.toList());

        System.out.println(collect);
        return collect;
    }




    /**
     * 为某个订单锁库存
     * <p>
     * <p>
     * 库存解锁的场景:
     * 1）下订单成功，超时未支付 | 用户点击了取消订单  库存应该自动解锁。
     * <p>
     * 2）库存锁定成功了，但是下面的业务出现异常了 导致订单回滚，我们的库存也应该自动解锁。
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean lockStock(WareSkuLockVo vo) {
        //锁库存之前应该先保存库存工作单和库存工作单详情
        WareOrderTaskEntity entity = new WareOrderTaskEntity();
        entity.setOrderSn(vo.getOrderSn());
        //保存库存工作单
        wareOrderTaskService.save(entity);

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
            if (wareIds == null || wareIds.size() <= 0) {
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {//订单项上的锁成功了
                    skuStocked = true;
                    WareOrderTaskDetailEntity detailEntity =
                            new WareOrderTaskDetailEntity
                                    (null, skuId, "", stock.getNum(), entity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(detailEntity);

                    StockLockedTo to = new StockLockedTo();
                    to.setTaskId(entity.getId());//工作单id
                    to.setDetailId(detailEntity.getId());//工作单详情id
                    //给MQ发一个库存锁定成功的消息
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked.stock",to);
                    break;
                } else {//失败了  重试下一个仓库

                }
            }
            //如果有他库存的仓库都不足卖的量了
            if (skuStocked == false) {
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

    @Override
    public void unlock(StockLockedTo to) {
        Long taskId = to.getTaskId();
        Long detailId = to.getDetailId();
        //查询数据库是否有库存项 如果有的话 说明库存锁成功了
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(to.getTaskId());
        if (taskDetailEntity != null){
            //解锁 看订单是否存在并检查订单的状态是否是已取消
            R r = orderFeignService.getStatus(taskEntity.getOrderSn());
            if(r.getCode() == 0){
                System.out.println("检查订单状态 || 看其是否存在");
                OrderVo data = r.getData(new TypeReference<OrderVo>(){});
                //如果订单都没有了或者订单是取消状态
                System.out.println(data);
                if (data == null || data.getStatus() == OrderStatusEnum.CANCLED.getCode()){
                    //如果订单已被取消了 才能解锁库存
                    WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(to.getDetailId());
                    //如果是锁定状态
                    if (byId.getLockStatus() == 1){
                        byId.setLockStatus(2);//
                        wareOrderTaskDetailService.updateById(byId);
                        unlockStock(byId.getSkuId(), byId.getSkuNum(), byId.getTaskId());
                    }
                }
            }else {
                throw new RuntimeException("远程服务调用异常！");
            }
        }


    }
    //进行了优化  原来是 skuId,ware_id,count,taskDetailId
    //虽然多了一次数据库交互 但是大大加快了查询速度用到了主键
    public void  unlockStock(Long sku_id,Integer count,Long taskDetailId){
        WareSkuEntity wareSkuEntity = this.baseMapper.selectOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", sku_id));

        wareSkuEntity.setStockLocked(wareSkuEntity.getStockLocked() - count);
        //解锁
        this.updateById(wareSkuEntity);
        System.out.println("=========库存成功解锁=======");

    }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

