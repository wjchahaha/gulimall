package com.jc.gulimall.gulimall.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-18 22:46
**/
@Data
public class UserLoginVo {
    @NotEmpty
    private String loginacct;
    @NotEmpty
    private String password;
}
