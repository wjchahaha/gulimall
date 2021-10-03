package com.jc.gulimall.product.web;

import com.jc.gulimall.product.entity.CategoryEntity;
import com.jc.gulimall.product.service.CategoryService;
import com.jc.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-09-25 17:17
**/
@Controller
public class IndexController {


    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> categorys=categoryService.getOneLevelCategory();

        model.addAttribute("categorys",categorys);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){

        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();

        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(Model model){
            return "hello";
    }

    }
