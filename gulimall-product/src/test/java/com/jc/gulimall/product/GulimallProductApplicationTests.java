package com.jc.gulimall.product;


import com.jc.gulimall.product.entity.BrandEntity;
import com.jc.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;
import java.util.Iterator;

/**
 * 1.引入oos-starter
 * 2.配置子账户信息 endpoint
 * 3.使用oosclient
 */
@SpringBootTest
class GulimallProductApplicationTests {


    @Autowired
    BrandService brandService;

//    @Resource
//    private OSSClient ossClient;



    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setName("华为");
//        brandEntity.setDescript("华为mate50");
//        brandEntity.setLogo("花花");
//
//        brandService.save(brandEntity);
//
//        System.out.println("保存成功！");


        Iterator<BrandEntity> iterator = brandService.list().iterator();

        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }

}
