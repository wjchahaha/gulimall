package com.jc.gulimall.product.dao;

import com.jc.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jc.gulimall.product.vo.SkuItemVo;
import com.jc.gulimall.product.vo.SpuItemAttrGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupAttrVo> getAttrGroupBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
