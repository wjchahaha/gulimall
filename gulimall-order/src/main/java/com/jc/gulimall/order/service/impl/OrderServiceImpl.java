package com.jc.gulimall.order.service.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.order.dao.OrderDao;
import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.service.OrderService;

@RabbitListener(queues = "jc.java.queue")
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 1.Message(org.springframework.amqp.core;) 原生消息 头 + 体
     * 2.T发送的消息类型
     * 3.Channel channel 当前传输数据的信道
     *
     * Queue 队列可以很多人来监听通过@RabbitListener 只要收到消息队列就删除消息 而且只能有一个收到消息
     * 1）加入订单服务有多个：同一个消息  只能有一个客户端收到
     * 2) 只有一个消息处理完成，我们才可以接到下一个消息
     * @param orderEntity
     * @param channel
     * @throws InterruptedException
     */
    @RabbitHandler
    public void listening(Message message,OrderEntity orderEntity, Channel channel) throws InterruptedException, IOException {
        System.out.println("接收到消息......"+orderEntity);
//        Thread.sleep(3000);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println(deliveryTag);
        //非批量模式
        if(deliveryTag % 2 == 0){
            System.out.println("签收了货物->"+message.getMessageProperties().getDeliveryTag());
            channel.basicAck(deliveryTag,false);
        }else{
            channel.basicReject(deliveryTag,true);
            System.out.println("没有签收货物->"+message.getMessageProperties().getDeliveryTag());
        }

        System.out.println("消息处理完成"+orderEntity.getBillContent());
    }

}