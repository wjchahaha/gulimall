package com.jc.gulimall.thirdparty.controller;

import com.jc.common.utils.R;
import com.jc.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-15 19:36
**/
@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    private SmsComponent smsComponent;

    /**
     * 提供给别得服务用的
     * @param phone
     * @param code
     */
    @PostMapping("/code")
    public R sms(@RequestParam("phone") String phone,@RequestParam("code") String code){
        smsComponent.sms(phone,"【创信】你的验证码是：5873，3分钟内有效！");
        System.out.println("手机号"+phone+"code"+code);
        return R.ok();
    }
}
