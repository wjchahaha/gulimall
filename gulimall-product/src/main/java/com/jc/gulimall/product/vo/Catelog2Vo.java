package com.jc.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-09-25 19:16
**/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo {
//         "catalog3List":Array[21],
//            "id":"62",
//            "name":"时尚女鞋"
    private String catalog1Id;
    private String id;
    private String name;

    List<Catelog3Vo> catalog3List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  Catelog3Vo{
        private String catalog2Id;
        private String id;
        private String name;

    }
}


