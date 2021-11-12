package com.jc.gulimall.product;


import com.jc.gulimall.product.entity.BrandEntity;
import com.jc.gulimall.product.service.AttrGroupService;
import com.jc.gulimall.product.service.BrandService;
import com.jc.gulimall.product.service.CategoryService;
import com.jc.gulimall.product.service.SkuSaleAttrValueService;
import com.jc.gulimall.product.vo.SkuItemSaleAttrVo;
import com.jc.gulimall.product.vo.SpuItemAttrGroupAttrVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Test
    public void test11(){
        List<SpuItemAttrGroupAttrVo> attrGroupBySpuId = attrGroupService.getAttrGroupBySpuId(3L, 225L);

        System.out.println(attrGroupBySpuId);
    }

    @Test
    public void testgetSkuSaleAttrBySpuId(){
        List<SkuItemSaleAttrVo> skuSaleAttrBySpuId = skuSaleAttrValueService.getSkuSaleAttrBySpuId(3L);
        System.out.println(skuSaleAttrBySpuId);
    }

    @Test
    public void testRedisson(){
        String s = stringRedisTemplate.opsForValue().get("category::getOneLevelCategory");
        System.out.println(s);
    }
    @Autowired
    private DataSource dataSource;

    @Test
    public void test31(){
        System.out.println("使用的连接池是"+dataSource);
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
