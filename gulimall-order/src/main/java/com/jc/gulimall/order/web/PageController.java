package com.jc.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 13:16
**/
@Controller
public class PageController {

    @GetMapping("/{path}.html")
    public String toPage(@PathVariable("path") String path){


        return path;
    }
}
