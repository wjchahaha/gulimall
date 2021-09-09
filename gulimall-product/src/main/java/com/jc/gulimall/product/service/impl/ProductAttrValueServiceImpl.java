package com.jc.gulimall.product.service.impl;

import com.jc.gulimall.product.entity.AttrEntity;
import com.jc.gulimall.product.service.AttrService;
import com.jc.gulimall.product.vo.BaseAttrs;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.ProductAttrValueDao;
import com.jc.gulimall.product.entity.ProductAttrValueEntity;
import com.jc.gulimall.product.service.ProductAttrValueService;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBaseAttrs(Long id, List<BaseAttrs> baseAttrs) {
        if(baseAttrs == null || baseAttrs.size() ==0)
            return;
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((item) -> {
            ProductAttrValueEntity entity = new ProductAttrValueEntity();
            entity.setAttrId(item.getAttrId());
            entity.setSpuId(id);
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            entity.setAttrName(attrEntity.getAttrName());
            entity.setAttrValue(item.getAttrValues());
            entity.setQuickShow(item.getShowDesc());
            return entity;
        }).collect(Collectors.toList());

        this.saveBatch(collect);
    }

}