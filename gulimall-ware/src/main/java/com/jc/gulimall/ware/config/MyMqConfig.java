package com.jc.gulimall.ware.config;



import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
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



    /**
     * 路由键：
     * stock.release.stock
     * stock.locked.stock
     *
     * 队列：
     * stock.delay.queue
     * stock.release.queue
     *
     *
     * @return
     */
    @Bean
    public TopicExchange stockEventExchange(){
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments

        TopicExchange topicExchange = new TopicExchange("stock-event-exchange",true,false,null);

        return topicExchange;
    }

    @Bean
    public Binding stockCreateStockBinding(){
//        String destination, DestinationType destinationType, String exchange, String routingKey,
//                @Nullable Map<String, Object> arguments
        Binding binding = new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked.stock",
                null);

        return binding;
    }

    @Bean
    public Queue stockDelayQueue(){
//        String name, boolean durable, boolean exclusive, boolean autoDelete,
//        @Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release.stock");
        arguments.put("x-message-ttl",60000 * 2 );
        Queue queue = new Queue("stock.delay.queue",true,false,false,arguments);

        return queue;
    }

    @Bean
    public Queue orderReleaseQueue(){

        Queue queue = new Queue("stock.release.queue",true,false,false,null);

        return queue;
    }



    @Bean
    public Binding stockReleaseOrderBinding(){
//        String destination, DestinationType destinationType, String exchange, String routingKey,
//                @Nullable Map<String, Object> arguments
        Binding binding = new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.stock.#",
                null);

        return binding;
    }

}
