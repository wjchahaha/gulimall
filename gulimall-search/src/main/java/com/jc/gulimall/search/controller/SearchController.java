package com.jc.gulimall.search.controller;

import com.jc.gulimall.search.service.SearchService;
import com.jc.gulimall.search.vo.SearchParam;
import com.jc.gulimall.search.vo.SearchRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-10-15 19:32
**/
@Controller
public class SearchController {


    @Autowired
    private SearchService searchService;

    @RequestMapping("/list.html")
    public String list(@RequestParam SearchParam vo, Model model){
        //去es中查返回页面需要的所有信息
       SearchRespVo res= searchService.search(vo);
       //放到Model中,
        model.addAttribute("result",res);
        return "list";
    }
}
