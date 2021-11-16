package com.jc.gulimall.gulimall.auth.feign;

import com.jc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-15 19:47
**/
@FeignClient("gulimall-third-party")
public interface ThirdPartyService {

    @PostMapping("/sms/code")
    public R sms(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
