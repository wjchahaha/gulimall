package com.jc.gulimall.cart.controller;

import com.jc.gulimall.cart.interceptor.CartInteceptor;
import com.jc.gulimall.cart.service.CartService;
import com.jc.gulimall.cart.vo.Cart;
import com.jc.gulimall.cart.vo.CartItem;
import com.jc.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-22 16:25
**/
@Controller
public class CartController {

    @Autowired
    private CartService cartService;
    /**
     * 在获取购物车前  判断用户是否登录
     * 1.登录
     * 2.未登录
     * 3.通过拦截器实现
     * @return
     */
    @GetMapping("/cartList")
    public String toCartList(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);

        return "cartList";
    }
    @ResponseBody
    @GetMapping("/getCartByUserId")
    public List<CartItem> getCartByUserId(){
        List<CartItem> cartByUserId = cartService.getCartByUserId();

        return cartByUserId;
    }


    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){

        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cartList";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("count") Integer count){
        cartService.countItem(skuId,count);

        return "redirect:http://cart.gulimall.com/cartList";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check)  {

        cartService.checkItem(skuId,check);

        return "redirect:http://cart.gulimall.com/cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") int num,
                            Model model) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId,num);
//        model.addAttribute("skuId",skuId);

        return "redirect:http://cart.gulimall.com/toCartSuccess.html?skuId="+skuId;
    }

    @GetMapping("/toCartSuccess.html")
    public String toCartSuccess(@RequestParam("skuId") Long skuId,Model model){
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",cartItem);
        return "success";
    }
}
