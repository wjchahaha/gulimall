package com.jc.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jc.common.utils.R;
import com.jc.gulimall.cart.feign.ProductFrignService;
import com.jc.gulimall.cart.interceptor.CartInteceptor;
import com.jc.gulimall.cart.service.CartService;
import com.jc.gulimall.cart.vo.CartItem;
import com.jc.gulimall.cart.vo.SkuInfoVo;
import com.jc.gulimall.cart.vo.UserInfoTo;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: gulimall
 * @description:
 * @author: Mr.Wang
 * @create: 2021-11-22 16:24
 **/
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFrignService productFrignService;
    private String CART_PREFIX = "gulimall:cart:";

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, int num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //远程查询当前要添加购物车的商品信息 用来返回
        String o = (String) cartOps.get(skuId.toString());
        if (o == null) {//添加新商品
            CartItem cartItem = new CartItem();
            R info = productFrignService.info(skuId);
            //获取商品详情
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                SkuInfoVo data = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());
                cartItem.setSkuId(skuId);
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setTotalPrice(cartItem.getTotalPrice());
            }, executor);

            CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                //获取商品销售属性
                List<String> list = productFrignService.skuSaleAttrValue(skuId);

                cartItem.setSkuAttr(list);
            }, executor);


            //等他俩全部完成
            CompletableFuture.allOf(task1, task2).get();

            String jsonString = JSONObject.toJSONString(cartItem);
            cartOps.put(skuId.toString(), jsonString);

            return cartItem;
        }
        else {
            //1.取出原来的数据
            CartItem cartItem = JSONObject.parseObject(o, CartItem.class);
            //2.删除原来的数据
            redisTemplate.delete(skuId.toString());
            //3.修改数据

            Integer newCount = cartItem.getCount() + num;
            BigDecimal newTotalPrice = cartItem.getPrice().multiply(new BigDecimal(String.valueOf(newCount)));
            cartItem.setCount(newCount);
            cartItem.setTotalPrice(newTotalPrice);
            //4.删除
            String jsonString = JSONObject.toJSONString(cartItem);
            cartOps.put(skuId.toString(), jsonString);
            return cartItem;
        }


    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSONObject.parseObject(o, CartItem.class);

        return cartItem;
    }

    /**
     * 获取到要操作的购物车
     *
     * @return
     */
    public BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInteceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {//说明用户登录了
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

        return operations;
    }
}
