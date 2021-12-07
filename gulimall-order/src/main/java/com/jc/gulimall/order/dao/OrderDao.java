package com.jc.gulimall.order.dao;

import com.jc.common.vo.MqMessage;
import com.jc.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.amqp.core.Message;

/**
 * 订单
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:29:04
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void saveMqMessage(@Param("statuc") String statusm,@Param("message_id") String message_id);

    void updateStatus(@Param("message_id") String messageId
            , @Param("status") String status
           );

    void saveMqToSent(@Param("mq") MqMessage mqMessage);
}
