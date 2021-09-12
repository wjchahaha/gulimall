package com.jc.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-09-12 10:31
**/
@Data
public class DoneVo {
    @NotNull
    private Long id;
    private List<Item> items;

}
