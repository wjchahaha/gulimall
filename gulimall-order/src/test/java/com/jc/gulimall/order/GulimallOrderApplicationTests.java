package com.jc.gulimall.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jc.common.utils.R;
import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.feign.MemberFeignService;
import com.jc.gulimall.order.vo.MemberAddressVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    /***
     * 1.如何创建交换器 Queue Binding？
     * 2.如何收发消息 RabbitmqAutoConfig已经为我们注入了一个Admin
     */

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MemberFeignService memberFeignService;
    @Test
    public void testGetAddress(){
        R info = memberFeignService.info(2L);
        Object memberReceiveAddress = info.get("memberReceiveAddress");
        String jsonString = JSONObject.toJSONString(memberReceiveAddress);
        MemberAddressVo addressVo = JSON.parseObject(jsonString, MemberAddressVo.class);

        System.out.println(1);
    }

    @Test
    public void testSendMessage(){
        for(int i = 0; i < 10; i++){
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setMemberUsername("wanghaha");
            orderEntity.setBillContent("鬼马-"+(i+1));
            rabbitTemplate.convertAndSend("jc.java.exchange","jc.java",orderEntity);

            log.info("消息发送成功{}",orderEntity);
        }
    }

    @Test
    void createExchange() {
        //DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        Exchange exchange = new DirectExchange("jc.java.exchange",true,false);
        amqpAdmin.declareExchange(exchange);

        log.info("交换机创建成功",exchange);

    }

    @Test
    void createQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments) {
        Queue queue = new Queue("jc.java.queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("队列创建成功",queue);
    }

    @Test
    void createBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments)
        Binding binding = new Binding("jc.java.queue",Binding.DestinationType.QUEUE,"jc.java.exchange","jc.java",null);
        amqpAdmin.declareBinding(binding);
        log.info("binding创建成功",binding);
    }


}
