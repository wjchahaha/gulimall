package com.jc.gulimall.product.dao;

import com.jc.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
