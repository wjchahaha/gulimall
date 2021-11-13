package com.jc.gulimall.product.web;

import com.jc.gulimall.product.service.SkuInfoService;
import com.jc.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-10-23 16:58
**/
@Controller
public class ItemController {


    @Autowired
    private SkuInfoService skuInfoService;
    /**
     * 展现当前sku详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo=skuInfoService.item(skuId);
        System.out.println("准备查询"+skuId+"的item");
        model.addAttribute("item",skuItemVo);
        System.out.println(skuItemVo);
        return "item";
    }
}
