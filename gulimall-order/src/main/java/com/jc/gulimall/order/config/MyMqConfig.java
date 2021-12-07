package com.jc.gulimall.order.config;


import com.jc.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-12-05 10:40
**/
@Configuration
public class MyMqConfig {


    @Bean
    public TopicExchange orderEventExchange(){
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments

        TopicExchange topicExchange = new TopicExchange("order-event-exchange",true,false,null);

        return topicExchange;
    }

    @Bean
    public Queue orderDelayQueue(){
//        String name, boolean durable, boolean exclusive, boolean autoDelete,
//        @Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        Queue queue = new Queue("order.delay.queue",true,false,false,arguments);

        return queue;
    }

    @Bean
    public Queue orderReleaseQueue(){

        Queue queue = new Queue("order.release.queue",true,false,false,null);

        return queue;
    }

    @Bean
    public Binding orderCreateOrderBinding(){
//        String destination, DestinationType destinationType, String exchange, String routingKey,
//                @Nullable Map<String, Object> arguments
        Binding binding = new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);

        return binding;
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
//        String destination, DestinationType destinationType, String exchange, String routingKey,
//                @Nullable Map<String, Object> arguments
        Binding binding = new Binding("order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);

        return binding;
    }

    @Bean
    public Binding orderReleaseStockBinding(){
//        String destination, DestinationType destinationType, String exchange, String routingKey,
//                @Nullable Map<String, Object> arguments
        Binding binding = new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other",
                null);

        return binding;
    }

}
