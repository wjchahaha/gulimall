package com.jc.gulimall.order.listener;

import com.jc.common.to.OrderTo;
import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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
* @create: 2021-12-06 12:43
**/
@RabbitListener( queues = "order.release.queue")
@Service
public class OrderCloseListener {

    @Autowired
    private RabbitTemplate template;
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        System.out.println("无效订单已收到,订单号："+orderEntity.getOrderSn());
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
        //订单释放了----->给库存解锁队列发一个消息
        OrderTo to = new OrderTo();
        BeanUtils.copyProperties(orderEntity,to);
        template.convertAndSend("order-event-exchange","order.release.other",to);
    }
}
