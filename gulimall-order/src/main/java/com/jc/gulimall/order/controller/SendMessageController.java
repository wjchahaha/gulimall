package com.jc.gulimall.order.controller;

import com.jc.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-25 20:18
**/
@RestController
public class SendMessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 交换器错了的话 correlationData====>null,ack===>true,消息null
     * 路由键错了的话 302 NO_ROuTE
     * @param num
     * @return
     */

    @GetMapping("/sendMessage")
    public String sendMessage(@RequestParam(value = "num",defaultValue = "10",required = false) Integer num){
        for(int i = 0; i < num; i++){
            if(i % 2 == 0) {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setMemberUsername("wanghaha");
                orderEntity.setBillContent("鬼马-" + (i + 1));
                rabbitTemplate.convertAndSend("jc.java.exchange", "jc.java", orderEntity);
            }else{

                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setMemberUsername("wanghaha");
                orderEntity.setBillContent("鬼马-" + (i + 1));
                rabbitTemplate.convertAndSend("jc.java.exchange", "jc.java", orderEntity);
            }
        }
        return "ok";
    }
}
