package com.jc.gulimall.coupon.dao;

import com.jc.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:23:20
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
