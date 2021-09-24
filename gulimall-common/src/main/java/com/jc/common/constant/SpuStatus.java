package com.jc.common.constant;

public enum SpuStatus {
    NEW_SPU(0,"新建状态"),SPU_UP(1,"商品上架"),SPU_DOWN(2,"商品下架");

    private int code;
    private String msg;

    SpuStatus(int code, String msg) {
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
