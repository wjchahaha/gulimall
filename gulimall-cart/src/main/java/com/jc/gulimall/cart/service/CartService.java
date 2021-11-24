package com.jc.gulimall.cart.service;

import com.jc.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-22 16:24
**/

public interface CartService {
    CartItem addToCart(Long skuId, int num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);
}
