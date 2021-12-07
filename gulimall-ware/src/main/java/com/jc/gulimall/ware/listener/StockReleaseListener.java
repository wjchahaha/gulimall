package com.jc.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.jc.common.constant.OrderStatusEnum;
import com.jc.common.to.OrderTo;
import com.jc.common.to.mq.StockLockedTo;
import com.jc.common.utils.R;
import com.jc.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.jc.gulimall.ware.entity.WareOrderTaskEntity;
import com.jc.gulimall.ware.entity.WareSkuEntity;
import com.jc.gulimall.ware.service.WareSkuService;
import com.jc.gulimall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-12-05 21:03
**/
@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleReleaseStock(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到库存解锁的消息--->看其是否回滚");
        try {
            wareSkuService.unlock(to);
            //无异常的话 自动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            System.out.println("已确认");
        }catch (Exception e){
            //有异常重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handleOrderReleaseStock(OrderTo to, Message message, Channel channel) throws IOException {

        try {
            wareSkuService.unLockByReleaseOrder(to);
            //无异常的话 自动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //有异常重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
