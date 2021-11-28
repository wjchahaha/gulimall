package com.jc.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 使用RabbitMQ
 * 1.引入ampq场景：RabbitAutoConfiguration
 * 2.给容器中自动配置了 RabbitTemplate AmqpAdmin,CacheConnectionFactory
 * 3.注解EnableRabbit 开启rabbit
 * 4.RabbitListener 标在方法和类上
 * 5.RabbitHandler 标在方法上 重载区分不同的消息
 *
 *

 */

@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
