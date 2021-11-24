package com.jc.common.exception;

/**
 * 10 通用
 * 11 商品
 * 12 订单
 * 13 购物车
 * 14 物流
 * 15 用户
 */
public enum BizCodeEnume {
    VAILD_EXCEPTION(10000,"参数格式异常"),
    UNKNOW_EXCEPTION(10001,"未知的异常"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    PHONE_EXIST_EXCEPTION(15001,"手机号已存在异常"),
    USERNAME_EXIST_EXCEPTION(15002,"用户名已存在异常"),
    LOGINACCT_PASSWORD_EXCEPTION(15003,"手机号或密码错误");

    private int code ;
    private String msg;

    BizCodeEnume(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
