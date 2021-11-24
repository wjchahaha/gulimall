package com.jc.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-22 13:52
 *
 * get必须重写
**/
public class Cart {
    List<CartItem> items;

    private Integer countNum;//商品总数

    private Integer countType;//商品类型总数

    private BigDecimal totalAmount;//商品总价格

    private BigDecimal reduce = new BigDecimal("0.00");//商品减免

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getCount();
        }
        return count;
    }


    public Integer getCountType() {
        int count = 0;
        for (CartItem item : items) {
            count++;
        }
        return count;
    }



    public BigDecimal getTotalAmount() {
        //1.购物项总价格
        BigDecimal total =  new BigDecimal("0");
        if(items != null && items.size() > 0) {
            for (CartItem item : items) {
                BigDecimal totalPrice = item.getTotalPrice();
                total = total.add(totalPrice);
            }
        }

        //2.减免价格
        total = total.subtract(reduce);
        return total;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
