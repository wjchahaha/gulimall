package com.jc.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.jc.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.jc.gulimall.product.dao.AttrDao;
import com.jc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.jc.gulimall.product.entity.AttrEntity;
import com.jc.gulimall.product.service.AttrService;
import com.jc.gulimall.product.vo.AttrVo;
import com.jc.gulimall.product.vo.SkuItemVo;
import com.jc.gulimall.product.vo.SpuItemAttrGroupAttrVo;
import com.jc.gulimall.product.vo.WithAttr;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.AttrGroupDao;
import com.jc.gulimall.product.entity.AttrGroupEntity;
import com.jc.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {


    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrDao attrDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        String key = (String) params.get("key");

        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }else{
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);

            return new PageUtils(page);
        }

    }

    @Override
    public void relationDelete(AttrAttrgroupRelationEntity[] entitys) {
//        attrAttrgroupRelationDao.delete(new QueryWrapper<>().eq("attr_id",1L).eq("attr_group_id",1))
        attrAttrgroupRelationDao.batchDeleteRelation(entitys);
    }

    @Override
    public List<WithAttr> allGroupAndRelationAttrByCatId(Long catelogId) {
        //?????????CatId??????????????????
        List<AttrGroupEntity> groups = attrGroupService.list(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //??????????????????????????????????????????
        List<WithAttr> data = groups.stream().map((attrGroup) -> {
            WithAttr attr = new WithAttr();
            BeanUtils.copyProperties(attrGroup,attr);

            List<AttrEntity> relationAttr = attrService.getRelationAttr(attrGroup.getAttrGroupId());

            attr.setAttrs(relationAttr);
            return attr;
        }).collect(Collectors.toList());

        return data;

    }

    @Override
    public List<SpuItemAttrGroupAttrVo> getAttrGroupBySpuId(Long spuId, Long catalogId) {
        AttrGroupDao baseMapper = this.baseMapper;
        List<SpuItemAttrGroupAttrVo> vos = baseMapper.getAttrGroupBySpuId(spuId,catalogId);
        return vos;
    }


}