package com.jc.gulimall.product.dao;

import com.jc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jc.gulimall.product.vo.AttrRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void batchDeleteRelation(@Param("entitys") AttrAttrgroupRelationEntity[] entitys);

    void saveBatch(@Param("vos") List<AttrRelationVo> vos);
}
