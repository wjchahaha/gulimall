package com.jc.gulimall.order.vo;

import com.jc.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 12:52
**/
@Data
public class SubmitOrderResVo {
    private OrderEntity orderEntity;//订单
    private Integer code;//返回状态码  1 令牌验证陈宫 只要不是0就失败了
}
