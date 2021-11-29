package com.jc.gulimall.cart.service;

import com.jc.gulimall.cart.vo.Cart;
import com.jc.gulimall.cart.vo.CartItem;

import java.util.List;
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

    Cart getCart() throws ExecutionException, InterruptedException;


    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer count);

    void deleteItem(Long skuId);

    List<CartItem> getCartByUserId();
}
