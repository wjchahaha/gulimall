package com.jc.common.constant;

public enum ProductAttrTypeEnum {
    ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALEm(0,"销售属性");

    private int code;
    private String msg;
    ProductAttrTypeEnum(int code,String msg){
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
