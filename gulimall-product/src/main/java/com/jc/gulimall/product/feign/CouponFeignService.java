package com.jc.gulimall.product.feign;

import com.jc.common.to.SkuReductionTo;
import com.jc.common.to.SpuBoundsTo;
import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * spring-cloud远程调用的过程
     * 1.@RequestBody将对象转为json放在请求体中，给对应的路径发送请求
     * 2.找到远程服务，请求体中有json数据
     * 3.接收方@RequestBody将json->对象
     * @param spuBoundsTo
     * @return
     */
    @RequestMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @RequestMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReductionTo(@RequestBody SkuReductionTo skuReductionTo);
}
