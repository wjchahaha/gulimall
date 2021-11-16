package com.jc.gulimall.member.exception;/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-16 22:30
**/
public class PhoneNoUniqueException extends RuntimeException {
    public PhoneNoUniqueException() {
        super("手机号已存在");
    }
}
