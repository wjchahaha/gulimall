package com.jc.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jc.common.constant.OrderConstant;
import com.jc.common.constant.OrderStatusEnum;
import com.jc.common.utils.R;
import com.jc.common.vo.MemberEntity;
import com.jc.common.vo.MqMessage;
import com.jc.gulimall.order.entity.OrderItemEntity;
import com.jc.gulimall.order.feign.CartFeignService;
import com.jc.gulimall.order.feign.MemberFeignService;
import com.jc.gulimall.order.feign.ProductFeignService;
import com.jc.gulimall.order.feign.WmsFeignService;
import com.jc.gulimall.order.interceptor.LoginUserInterceptor;
import com.jc.gulimall.order.service.OrderItemService;
import com.jc.gulimall.order.to.OrderCreateTo;
import com.jc.gulimall.order.vo.*;
import com.rabbitmq.client.Channel;
import org.aspectj.weaver.ast.Or;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.swing.*;
import javax.xml.crypto.Data;

//@RabbitListener(queues = "jc.java.queue")
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
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
            //远程服务去查询是否有库存
            R r = wmsFeignService.hasStockBySkuIds(collect);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {});
            if(data != null){
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::isHasStock));
                vo.setStocks(collect1);
                System.out.println(collect1);
            }
        },executor);
        //TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        vo.setOrderToken(token);
        stringRedisTemplate.opsForValue().set(OrderConstant.ORDER_USER_TOKEN_PRE+memberEntity.getId(),token);
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

    //本地事务： 在分布式系统中 只能控制住自己服务的回滚 控制不了其他服务的回滚
    //分布式事务：网络抖动等原因。
    @Transactional
    @Override
    public SubmitOrderResVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResVo response = new SubmitOrderResVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1])  else  return 0 end";
        //1.验证令牌
        String orderToken = vo.getOrderToken();
        String token = stringRedisTemplate.opsForValue().get(OrderConstant.ORDER_USER_TOKEN_PRE + memberEntity.getId());
        //返回0删锁失败  1删除成功

        //原子验证和删除令牌
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.ORDER_USER_TOKEN_PRE + memberEntity.getId()), orderToken);
        if (execute == 1L){
            //succes 1.创建订单
            response.setCode(1);
            OrderCreateTo order = createOrder(vo);
            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();//65985
            if(Math.abs(payAmount.subtract(payPrice).doubleValue()) < 10){
                //验价成功
                //3.TODO 保存订单 以及订单详情
                save(order);

                //锁定库存  有异常的话回滚
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setLocks(collect);
                wareSkuLockVo.setOrderSn(order.getOrderEntity().getOrderSn());
                //TODO 4.远程锁库存
                // cause:锁库存成功了 但是网络抖动返回超时了  会导致订单回滚 库存不会回滚
                //为了保证高并发 库存得自己回滚
                R r = wmsFeignService.lockStock(wareSkuLockVo);
                //主要是怕库存锁定成功了 后面的代码出问题 比如远程服务返回出问题 或者 后面扣减积分出问题
                //导致订单以及订单详情回滚库存不回滚的情况

                if (r.getCode() == 0){//锁定成功了
                    response.setOrderEntity(order.getOrderEntity());
                    //订单创建成功了 发送给延迟队列消息
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrderEntity());


                    return response;
                }
                else{
                    response.setCode(3);//3是没库存了
                    throw new RuntimeException("没库存了");
                }
                //订单号 -->所有订单项
            }else{
                //失败
                response.setCode(2);//金额对比失败
                return  response;
            }
        }
        else{
            response.setCode(4);
            return response;
        }

    }


    @Override
    public OrderEntity getStatus(String orderSn) {
        QueryWrapper<OrderEntity> order_sn = new QueryWrapper<OrderEntity>().eq("order_sn", orderSn);
        OrderEntity one = getOne(order_sn);
        return one;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //从数据库查询订单状态
        OrderEntity byId = this.getById(orderEntity.getId());
        //超时未支付
        if (byId == null ||byId.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){

            OrderEntity entity = new OrderEntity();
            entity.setStatus(OrderStatusEnum.CANCLED.getCode());
            entity.setId(orderEntity.getId());
            this.updateById(entity);
        }
    }


    private void save(OrderCreateTo order) {
        //保存订单
        OrderEntity orderEntity = order.getOrderEntity();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        //保存订单项
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);

    }

    private OrderCreateTo createOrder(OrderSubmitVo vo) {
        OrderCreateTo res = new OrderCreateTo();
        String orderSn = IdWorker.getTimeId();
        //构建订单的收货地址信息 收货人信息 运费
        OrderEntity orderEntity = buildOrder(vo, orderSn);
        //构建所有订单项
        List<OrderItemEntity> allItems = buildOrderAllItems(orderSn);
        //验价
        computePrice(orderEntity,allItems);
        res.setOrderEntity(orderEntity);//在验价之后
        res.setOrderItems(allItems);
        System.out.println(res);
        return res;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> allItems) {
        //订单总额
        BigDecimal total = new BigDecimal("0.00");
        BigDecimal coupon = new BigDecimal("0.00");
        BigDecimal integration = new BigDecimal("0.00");
        BigDecimal promotion = new BigDecimal("0.00");
        BigDecimal giftGrowth = new BigDecimal("0.00");
        BigDecimal giftIntegration = new BigDecimal("0.00");
        for (OrderItemEntity item : allItems) {
            //订单项总和
            total = total.add(item.getRealAmount());

            //优惠
            coupon = coupon.add(item.getCouponAmount());
            integration = integration.add(item.getIntegrationAmount());
            promotion = promotion.add(item.getPromotionAmount());

            //成长值
            giftGrowth = giftGrowth.add(new BigDecimal(item.getGiftGrowth().toString()));
            giftIntegration = giftIntegration.add(new BigDecimal(item.getGiftIntegration().toString()));
        }
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        //优惠总额
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        //设置成长值
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setGrowth(giftGrowth.intValue());
        orderEntity.setDeleteStatus(0);//未删除

    }

    /**
     * 构建订单中的所有订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderAllItems(String orderSn) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        List<OrderItemVo> cart = cartFeignService.getCartByUserId();
        if(cart != null && cart.size() > 0){
            List<OrderItemEntity> collect = cart.stream().map((cartItem) -> {
                OrderItemEntity item = buildOrderItem(cartItem);
                //订单号
                item.setOrderSn(orderSn);
                return item;
            }).collect(Collectors.toList());
            return  collect;
        }
        return null;
    }

    /**
     * 构建订单中的订单项信息
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity entity = new OrderItemEntity();

        //订单号信息

        //spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuBySkuId(skuId);
        SpuInfoVo spu = r.getData(new TypeReference<SpuInfoVo>() {});
        entity.setSpuId(spu.getId());
        entity.setSpuName(spu.getSpuName());
        entity.setSpuBrand(spu.getBrandId().toString());
        entity.setCategoryId(spu.getCatalogId());
        //sku信息
        entity.setSkuPrice(cartItem.getPrice());
        entity.setSkuId(cartItem.getSkuId());
        entity.setSkuName(cartItem.getTitle());;
        entity.setSkuQuantity(cartItem.getCount());
        entity.setSkuPic(cartItem.getImage());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        entity.setSkuAttrsVals(skuAttr);
        //积分信息
        entity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        entity.setGiftIntegration(cartItem.getPrice().intValue());
        //优惠信息

        entity.setIntegrationAmount(new BigDecimal("0.0"));
        entity.setCouponAmount(new BigDecimal("0.0"));
        entity.setPromotionAmount(new BigDecimal("0.0"));
        //真实价格等于 老价格减去优惠的价格
        BigDecimal old = entity.getSkuPrice().multiply(new BigDecimal(entity.getSkuQuantity().toString()));
        BigDecimal subtract = old.subtract(entity.getCouponAmount()).subtract(entity.getIntegrationAmount()).subtract(entity.getPromotionAmount());
        entity.setRealAmount(subtract);
        return entity;
    }

    /**
     * 构建订单的收货地址信息 收货人信息 运费
     * @param vo
     * @param timeId
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVo vo,String timeId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(timeId);
        //获取收货地址信息
        R info = memberFeignService.info(vo.getAddrId());
        Object memberReceiveAddress = info.get("memberReceiveAddress");
        String jsonString = JSONObject.toJSONString(memberReceiveAddress);
        MemberAddressVo addressVo = JSON.parseObject(jsonString, MemberAddressVo.class);

        if (addressVo != null){
        orderEntity.setReceiverProvince(addressVo.getProvince());
        orderEntity.setReceiverCity(addressVo.getCity());
        orderEntity.setReceiverRegion(addressVo.getRegion());
        orderEntity.setReceiverPostCode(addressVo.getPostCode());
        orderEntity.setReceiverDetailAddress(addressVo.getDetailAddress());
        orderEntity.setReceiverName(addressVo.getName());
        //收货人信息
        orderEntity.setMemberUsername(addressVo.getName());
        orderEntity.setMemberId(addressVo.getMemberId());
        orderEntity.setReceiverPhone(addressVo.getPhone());
        //运费
        orderEntity.setFreightAmount(new BigDecimal("9"));

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        }

        return orderEntity;
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
//    @RabbitHandler
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