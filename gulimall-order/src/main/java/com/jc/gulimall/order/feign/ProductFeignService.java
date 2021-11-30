package com.jc.gulimall.order.feign;

import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-30 20:32
**/
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuBySkuId(@PathVariable("id") Long skuId);
}
