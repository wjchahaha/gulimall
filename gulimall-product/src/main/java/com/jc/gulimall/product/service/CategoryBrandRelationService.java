package com.jc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.product.entity.BrandEntity;
import com.jc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.jc.gulimall.product.vo.BrandResVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);


    List<BrandEntity> selectBrandsByCatId(Long catId);
}

