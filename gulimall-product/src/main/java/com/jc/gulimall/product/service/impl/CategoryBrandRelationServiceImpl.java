package com.jc.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jc.gulimall.product.dao.BrandDao;
import com.jc.gulimall.product.dao.CategoryDao;
import com.jc.gulimall.product.entity.BrandEntity;
import com.jc.gulimall.product.entity.CategoryEntity;
import com.jc.gulimall.product.vo.BrandResVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.CategoryBrandRelationDao;
import com.jc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.jc.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
            Long brandId = categoryBrandRelation.getBrandId();
            Long catelogId = categoryBrandRelation.getCatelogId();

            BrandEntity brandEntity = brandDao.selectById(brandId);
            CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

            categoryBrandRelation.setBrandName(brandEntity.getName());
            categoryBrandRelation.setCatelogName(categoryEntity.getName());

            this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {

        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setBrandName(name);

        categoryBrandRelationDao.update(entity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<BrandEntity> selectBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> catelog_id = categoryBrandRelationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        //将等于此分类的关联关系全都查出来获取brand_id
        if (catelog_id == null){
            return new ArrayList<>();
        }
        List<Long> collect = catelog_id.stream().map((item) -> {
            return item.getBrandId();
        }).collect(Collectors.toList());
        //进行优化
        List<BrandEntity> brandEntities = brandDao.selectBatchIds(collect);
//        List<BrandEntity> brand_id = brandDao.selectList(new QueryWrapper<BrandEntity>().in("brand_id", collect));
        //查询brandEntity
        return brandEntities;
    }


}