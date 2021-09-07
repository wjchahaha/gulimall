package com.jc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.jc.gulimall.product.entity.AttrGroupEntity;
import com.jc.gulimall.product.vo.AttrVo;
import com.jc.gulimall.product.vo.WithAttr;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);


    void relationDelete(AttrAttrgroupRelationEntity[] entitys);

    List<WithAttr> allGroupAndRelationAttrByCatId(Long catelogId);
}

