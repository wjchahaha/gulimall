package com.jc.gulimall.search.controller;

import com.jc.common.exception.BizCodeEnume;
import com.jc.common.to.SkuEsModel;
import com.jc.common.utils.R;
import com.jc.gulimall.search.constant.ElasticSearchConstant;
import com.jc.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-09-23 22:18
**/
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ProductUpController {


    @Autowired
    private ProductService productService;

    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean flag = false;
        try {
            flag = productService.productUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSearchController商品上架错误：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if(flag){
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }else{
            return R.ok();
        }


    }
}
