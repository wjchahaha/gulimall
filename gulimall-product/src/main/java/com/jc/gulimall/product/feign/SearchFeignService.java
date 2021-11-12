package com.jc.gulimall.product.feign;

import com.jc.common.to.SkuEsModel;
import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-09-23 22:48
**/
@FeignClient("gulimall-search")
public interface SearchFeignService {
    
    @PostMapping("/search/save/product")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModels);
}
