package com.jc.gulimall.order.web;

import com.jc.gulimall.order.entity.OrderEntity;
import com.jc.gulimall.order.service.OrderService;
import com.jc.gulimall.order.vo.OrderConfirmVo;
import com.jc.gulimall.order.vo.OrderSubmitVo;
import com.jc.gulimall.order.vo.SubmitOrderResVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 16:54
**/
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toConfirm(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = orderService.getOrderConfirmData();

        model.addAttribute("orderConfirmData",orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo){

        System.out.println("前台传过来的订单的数据"+vo);
        //创建订单->验价格->锁库存
        SubmitOrderResVo resVo = orderService.submitOrder(vo);
        //成功 到支付选项页
        if (resVo.getCode() == 1){
            return "pay";
        }
        //失败 到订单确认页重新确认
        return "redirect:http://order.gulimall.com/confirm.html";
    }
}
