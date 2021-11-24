package com.jc.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-22 16:37
**/
@ToString
@Data
public class UserInfoTo {
    //如果用户id不空  则已登录
    private Long userId;
    private String userKey;

    private boolean tempUser;

}
