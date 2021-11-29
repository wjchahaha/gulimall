package com.jc.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.vo.OrderConfirmVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:29:04
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo getOrderConfirmData() throws ExecutionException, InterruptedException;
}

