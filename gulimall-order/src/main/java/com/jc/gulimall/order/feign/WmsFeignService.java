package com.jc.gulimall.order.feign;

import com.jc.common.utils.R;
import com.jc.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R hasStockBySkuIds(@RequestBody List<Long> skuIds);

    @PostMapping("/ware/waresku/order/lockStock")
    R lockStock(@RequestBody WareSkuLockVo vo);

}
