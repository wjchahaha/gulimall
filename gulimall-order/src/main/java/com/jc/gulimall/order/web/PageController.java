package com.jc.gulimall.order.web;

import com.jc.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 13:16
**/
@Controller
public class PageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{path}.html")
    public String toPage(@PathVariable("path") String path){


        return path;
    }

    @ResponseBody
    @GetMapping("/sendOrder")
    public String sendOrder() throws InterruptedException {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());

        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        return "ok";
    }
}
