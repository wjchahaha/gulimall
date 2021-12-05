package com.jc.gulimall.ware.feign;

import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("order/order/getStatus/{orderSn}")
    R getStatus(@PathVariable("orderSn") String orderSn);
}
