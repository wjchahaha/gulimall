package com.jc.gulimall.ware.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-25 17:22
**/
@Configuration
public class MyRabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitMQTemplate
     *  ===========消息确认机器-可靠抵达=============
     * RabbitmqTemplate
     * 1.交换器收到消息就回调
     *   1)spring.rabbitmq.publisher-returns=true
     *   2)定制我们的rabbitmqTemplate,为Rabbitmq中的ConfirmCallback设置上,它是一个接口.
     * 2.队列收到消息就回调
     *   1)spring.rabbitmq.publisher-returns=true
     *   2)定制我们的rabbitmqTemplate,为Rabbitmq中的设置上,它是一个接口.
     *
     * 3.消费端再次确认(保证每个消息被正确消费，才让队列删除这个消息） 这样就保证了 消费被成功消费了 不成功消费就回调让其重发
     *   spring.rabbitmq.listener.simple.acknowledge-mode=manual
     *   1)默认是自动确认的 只要消费端收到消息了 就会自动回复给服务器，队列就会移除这个消息。
     *          问题：收到很多消息的话  自动恢复给服务器了 只有一个消息处理完的话 机房失火了 发生消息丢失
     *    解决方案：自动->手动确认
     *    1）只要消息不手动确认的话 消息就一直是unacked状态， 即使机房失火的话 消息不会丢失 消息会从unack——>ready状态
     *    2）等下次在有消费者进来的话  就发给他
     *    如何手动确认?
     *      channel.basicAck(deliveryTag,false);       签收 业务成功
     *      channel.basicNack(deliveryTag,false,true); 拒签 业务失败
     *
     */
//    MyRabbitConfig执行完构造函数后执行这个函数
    @PostConstruct
    public void initRabbitTemplate(){

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达ack==true
             * @param correlationData 当前关联的唯一关联数据
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("correlationData====>"+correlationData+",ack===>"+ack+",消息"+cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只有消息没有投递给指定的队列 就触发这个失败回调
             * @param message  投递失败信息的详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange  消息所属交换机
             * @param routingKey 消息的路由键
             *
             *
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //312  noRoute
                System.out.println("Fail Message===>"+message+",replyCode===>"+replyCode+",replyText"+replyText+",exchange===>"+exchange+",routingKey"+routingKey);
            }
        });
    }
}
