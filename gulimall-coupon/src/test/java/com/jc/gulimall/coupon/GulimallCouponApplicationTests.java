package com.jc.gulimall.coupon;






import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.jc.gulimall.coupon.entity.CouponEntity;
import com.jc.gulimall.coupon.service.CouponService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class GulimallCouponApplicationTests {

    @Autowired
    CouponService couponService;

    @Test
    public void contextLoads() {
        QueryChainWrapper<CouponEntity> query = couponService.query();

        int count = couponService.count();

        System.out.println(count);

    }

}
