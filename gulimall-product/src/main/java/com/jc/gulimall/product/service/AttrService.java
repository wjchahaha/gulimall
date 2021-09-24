package com.jc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.product.entity.AttrEntity;
import com.jc.gulimall.product.entity.ProductAttrValueEntity;
import com.jc.gulimall.product.vo.AttrRespVo;
import com.jc.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType);

    void saveAttrVo(AttrVo attrVo);

    AttrRespVo selectAttrRespVo(Long attrId);

    void updateDetail(AttrRespVo vo);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils getOtherAttr(Map<String, Object> params, Long attrgroupId);

    PageUtils getOtherAttr2(Map<String, Object> params, Long attrgroupId);

    void updateBySpuId(Long spuId, List<ProductAttrValueEntity> entities);

    List<AttrEntity> selectSearchAttrs(List<Long> collect1);

    List<Long> selectSearchAttrsIds(List<Long> attrids);


//    PageUtils querySalePage(Map<String, Object> params, Long catelogId);
}

