package com.jc.gulimall.order.feign;

import com.jc.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 19:30
**/
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/getCartByUserId")
    public List<OrderItemVo> getCartByUserId();
}
