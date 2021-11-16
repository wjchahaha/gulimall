package com.jc.gulimall.member.exception;

import java.util.concurrent.Executors;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-16 22:30
**/
public class UserNameNoUniqueException extends RuntimeException {
    public UserNameNoUniqueException() {
        super("用户名已存在");
    }
}
