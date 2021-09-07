package com.jc.gulimall.product.service.impl;

import com.jc.gulimall.product.vo.AttrRelationVo;
import org.springframework.beans.BeanUtils;
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

import com.jc.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.jc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.jc.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatch(List<AttrRelationVo> vos) {
        List<AttrAttrgroupRelationEntity> entities = new ArrayList<>();

        List<AttrAttrgroupRelationEntity> collect = vos.stream().map((item) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            //对拷
            BeanUtils.copyProperties(item, entity);
            return entity;
        }).collect(Collectors.toList());

        this.saveBatch(collect);
    }

    @Override
    public void saveBatch2(List<AttrAttrgroupRelationEntity> entities) {
        this.saveBatch(entities);
    }

}