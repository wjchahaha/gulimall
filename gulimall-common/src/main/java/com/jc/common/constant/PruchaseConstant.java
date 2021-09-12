package com.jc.common.constant;

public enum PruchaseConstant {

    CREATEED(0,"新建"),
    ALLOTED(1,"已分配"),
    RECEIVED(2,"已领取"),
    FINISHED(3,"已完成"),
    ERROR(4,"有异常");
    private int code;
    private String msg;
    PruchaseConstant(int code, String msg){
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
