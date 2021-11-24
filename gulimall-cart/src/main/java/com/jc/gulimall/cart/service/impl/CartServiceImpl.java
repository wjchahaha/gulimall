package com.jc.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jc.common.utils.R;
import com.jc.gulimall.cart.feign.ProductFrignService;
import com.jc.gulimall.cart.interceptor.CartInteceptor;
import com.jc.gulimall.cart.service.CartService;
import com.jc.gulimall.cart.vo.Cart;
import com.jc.gulimall.cart.vo.CartItem;
import com.jc.gulimall.cart.vo.SkuInfoVo;
import com.jc.gulimall.cart.vo.UserInfoTo;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        //1.获取用户信息判断是否登录
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInteceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){//登录了
            //1.1获取临时购物车
            List<CartItem> tempCart = getCart(CART_PREFIX + userInfoTo.getUserKey());
            if(tempCart != null && tempCart.size() > 0){
                //1.2登录情况下 合并临时购物车的数据
                for (CartItem cartItem : tempCart) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
            }
            //1.3合并完了 在获取登录后的购物车
            cart.setItems(getCart(CART_PREFIX + userInfoTo.getUserId()));
            cart.setTotalAmount(cart.getTotalAmount());
            //1.4删除临时购物车的数据

            if(deleteCart(CART_PREFIX+userInfoTo.getUserKey())){
                System.out.println("删除临时购物车成功");
            }

        }else{//没登录
            cart.setItems(getCart(CART_PREFIX + userInfoTo.getUserKey()));
            cart.setTotalAmount(cart.getTotalAmount());
        }



        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        CartItem cartItem = getCartItem(skuId);

        cartItem.setCheck(check == 1? true : false);
        cartOps.put(skuId.toString(),JSONObject.toJSONString(cartItem));

    }

    @Override
    public void countItem(Long skuId, Integer count) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);

        cartItem.setCount(count);

        cartOps.put(skuId.toString(), JSONObject.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    public boolean deleteCart(String key){
        Boolean delete = redisTemplate.delete(key);
        return delete;
    }

    public List<CartItem> getCart(String cartKey){
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();

        if(values != null && values.size() > 0){
        List<CartItem> collect = values.stream().map((object) -> {
            String str = (String) object;
            CartItem cartItem = JSONObject.parseObject(str, CartItem.class);
            return cartItem;
        }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    public List<CartItem> getCartItems(){
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        List<Object> cartItems = cartOps.values();
        if (cartItems != null && cartItems.size() > 0) {
            List<CartItem> collect = cartItems.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSONObject.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());

            return collect;
        }

        return null;
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
