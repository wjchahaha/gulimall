package com.jc.gulimall.product.feign;

import com.jc.common.to.SkuHasStockVo;
import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {


    @PostMapping("/ware/waresku/hasStock")
    public R hasStockBySkuIds(@RequestBody List<Long> skuIds);
}
