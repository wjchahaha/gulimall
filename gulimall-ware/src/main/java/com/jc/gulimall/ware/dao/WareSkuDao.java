package com.jc.gulimall.ware.dao;

import com.jc.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * εεεΊε­
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:34:53
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStack(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);


    Long getSkuWare(@Param("skuId") Long skuId);

    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("num") Integer num);
}
