package com.jc.gulimall.product.dao;

import com.jc.gulimall.product.entity.BrandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@Mapper
public interface BrandDao extends BaseMapper<BrandEntity> {

    void updateByid(BrandEntity brandEntity);
}
