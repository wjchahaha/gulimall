package com.jc.common.constant;

public enum PurchaseDetailConstant {
    CREATE(0,"新建"),
    ALLOTED(1,"已分配"),
    BUYING(2,"正在采购"),
    FINISHED(3,"已完成"),
    BUYERROR(4,"采购失败");
    private int code;
    private String msg;
    PurchaseDetailConstant(int code,String msg){
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
