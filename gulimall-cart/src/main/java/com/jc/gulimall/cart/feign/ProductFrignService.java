package com.jc.gulimall.cart.feign;

import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFrignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/skusaleattrvalue/SkuSaleAttrValue/{skuId}")
    public List<String> skuSaleAttrValue(@PathVariable("skuId") Long skuId);
}
