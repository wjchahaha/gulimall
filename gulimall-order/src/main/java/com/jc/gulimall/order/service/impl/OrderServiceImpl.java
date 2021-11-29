package com.jc.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.jc.common.utils.R;
import com.jc.common.vo.MemberEntity;
import com.jc.gulimall.order.feign.CartFeignService;
import com.jc.gulimall.order.feign.MemberFeignService;
import com.jc.gulimall.order.feign.WmsFeignService;
import com.jc.gulimall.order.interceptor.LoginUserInterceptor;
import com.jc.gulimall.order.vo.MemberAddressVo;
import com.jc.gulimall.order.vo.OrderConfirmVo;
import com.jc.gulimall.order.vo.OrderItemVo;
import com.jc.gulimall.order.vo.SkuStockVo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.aspectj.weaver.ast.Or;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.order.dao.OrderDao;
import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@RabbitListener(queues = "jc.java.queue")
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private WmsFeignService wmsFeignService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberFeignService memberFeignService;


    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getOrderConfirmData() throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //老请求中的数据 在异步调用之前把他拿出来
        RequestAttributes oldRequestAttributes = RequestContextHolder.getRequestAttributes();
        //地址信息 远程调用
//        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
//        List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
//        vo.setAddress(address);
        CompletableFuture<Void> addressfuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(oldRequestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            vo.setAddress(address);
        }, executor);

        //订单项  远程调取购物车中选中的购物想
        //feign在远程调用之前要构造请求调用很多的拦截器

//        List<OrderItemVo> cartByUserId = cartFeignService.getCartByUserId();
//        vo.setItems(cartByUserId);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(oldRequestAttributes);
            List<OrderItemVo> cartByUserId = cartFeignService.getCartByUserId();
            vo.setItems(cartByUserId);
        }, executor).thenRunAsync(()->{
            //查询库存
            List<OrderItemVo> items = vo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            //远程服务去查询

            R r = wmsFeignService.hasStockBySkuIds(collect);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {});
            if(data != null){
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::isHasStock));
                vo.setStocks(collect1);
                System.out.println(collect1);
            }
        },executor);
        //TODO 发票记录

        //优惠券积分等信息
        Integer integration = memberEntity.getIntegration();
        vo.setIntegration(integration);

        //订单总金额

        CompletableFuture.allOf(addressfuture,cartFuture).get();
        vo.setOrderTotalAmount(vo.getOrderTotalAmount());
        //应付总金额
        vo.setOrderPayAmount(vo.getOrderTotalAmount());

        System.out.println(vo);
        return vo;
    }

    /**
     * 1.Message(org.springframework.amqp.core;) 原生消息 头 + 体
     * 2.T发送的消息类型
     * 3.Channel channel 当前传输数据的信道
     *
     * Queue 队列可以很多人来监听通过@RabbitListener 只要收到消息队列就删除消息 而且只能有一个收到消息
     * 1）加入订单服务有多个：同一个消息  只能有一个客户端收到
     * 2) 只有一个消息处理完成，我们才可以接到下一个消息
     * @param orderEntity
     * @param channel
     * @throws InterruptedException
     */
    @RabbitHandler
    public void listening(Message message,OrderEntity orderEntity, Channel channel) throws InterruptedException, IOException {
        System.out.println("接收到消息......"+orderEntity);
//        Thread.sleep(3000);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println(deliveryTag);
        //非批量模式
        if(deliveryTag % 2 == 0){
            System.out.println("签收了货物->"+message.getMessageProperties().getDeliveryTag());
            channel.basicAck(deliveryTag,false);
        }else{
            channel.basicReject(deliveryTag,true);
            System.out.println("没有签收货物->"+message.getMessageProperties().getDeliveryTag());
        }

        System.out.println("消息处理完成"+orderEntity.getBillContent());
    }

}