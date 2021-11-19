package com.jc.gulimall.member.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-18 23:16
**/
@Data
public class MemberLoginVo {
    @NotEmpty
    private String loginacct;
    @NotEmpty
    private String password;
}
