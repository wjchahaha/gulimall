package com.jc.gulimall.product;


import com.jc.gulimall.product.entity.BrandEntity;
import com.jc.gulimall.product.service.BrandService;
import com.jc.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

/**
 * 1.引入oos-starter
 * 2.配置子账户信息 endpoint
 * 3.使用oosclient
 */
@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {


    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }
    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","world"+ UUID.randomUUID().toString());

        String hello = ops.get("hello");

        System.out.println("从缓存中拿到的数据:"+hello);
    }

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径:{}", Arrays.asList(catelogPath));
    }

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
