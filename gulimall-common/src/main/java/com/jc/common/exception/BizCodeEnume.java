package com.jc.common.exception;

/**
 * 10 通用
 * 11 商品
 * 12 订单
 * 13 购物车
 * 14 物流
 */
public enum BizCodeEnume {
    VAILD_EXCEPTION(10000,"参数格式异常"),
    UNKNOW_EXCEPTION(10001,"未知的异常"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private int code;
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
