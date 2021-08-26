package com.jc.common.exception;


public enum BizCodeEnume {
    VAILD_EXCEPTION(10000,"参数格式异常"),
    UNKNOW_EXCEPTION(10001,"未知的异常");

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
