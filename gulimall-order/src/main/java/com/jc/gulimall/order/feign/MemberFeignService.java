package com.jc.gulimall.order.feign;

import com.jc.common.utils.R;
import com.jc.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 18:30
**/
@FeignClient("gulimall-member")
public interface MemberFeignService {


    @GetMapping("/member/member/{memberId}/getAddress")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

    /**
     * 根据收货地址id获取收货地址信息
     * @param id
     * @return
     */
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}
