package com.jc.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jc.common.constant.ProductAttrTypeEnum;
import com.jc.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.jc.gulimall.product.dao.AttrGroupDao;
import com.jc.gulimall.product.dao.CategoryDao;
import com.jc.gulimall.product.entity.*;
import com.jc.gulimall.product.service.CategoryService;
import com.jc.gulimall.product.service.ProductAttrValueService;
import com.jc.gulimall.product.vo.AttrRespVo;
import com.jc.gulimall.product.vo.AttrVo;
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

import com.jc.gulimall.product.dao.AttrDao;
import com.jc.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> waWrapper = new QueryWrapper<AttrEntity>().eq("attr_type",
                "base".equalsIgnoreCase(attrType)?ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode():ProductAttrTypeEnum.ATTR_TYPE_SALEm.getCode());

        if (catelogId != 0) {//catelogId != 0
            // ????????? base ?????????????????? ??????1 ????????????0
            waWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            waWrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                waWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        // ?????????????????????????????????????????? ?????????
        List<AttrEntity> records = page.getRecords();
        // attrRespVos ????????????????????????Vo
        List<AttrRespVo> attrRespVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            // 1.??????????????????????????????  ????????????????????????  ???attrRespVo ??????????????????
            if("base".equalsIgnoreCase(attrType)){
                // attr??????????????? ??????????????????????????????????????????
                AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (entity != null && entity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity);
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            // 2.????????????id ???attrRespVo ????????????????????????
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrRespVos);
        return pageUtils;


    }

    @Override
    public void saveAttrVo(AttrVo attrVo) {
        //????????????
        AttrEntity attrEntity = new AttrEntity();

        BeanUtils.copyProperties(attrVo, attrEntity);

        this.save(attrEntity);
        //???????????????????????????????????????
        if (attrVo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId() != null) {


            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();

            entity.setAttrId(attrEntity.getAttrId());
            entity.setAttrGroupId(attrVo.getAttrGroupId());

            attrAttrgroupRelationDao.insert(entity);
        }
    }

    @Override
    public AttrRespVo selectAttrRespVo(Long attrId) {

        AttrEntity attrEntity = this.getById(attrId);

        AttrRespVo attrRespVo = new AttrRespVo();

        BeanUtils.copyProperties(attrEntity, attrRespVo);
        if (attrEntity.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.selectById(attrId);

            //?????????????????????
            if (entity != null) {
                //??? vo????????????id
                attrRespVo.setAttrGroupId(entity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity.getAttrGroupId());
                //??????????????????
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        //??????path
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(attrId);
        if (categoryEntity != null)
            attrRespVo.setCatelogName(categoryEntity.getName());
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateDetail(AttrRespVo vo) {

        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        this.updateById(attrEntity);

//        AttrGroupEntity attrGroupEntity = new AttrGroupEntity();
//        BeanUtils.copyProperties(vo,attrGroupEntity);
//        //????????????????????????
//        attrGroupDao.updateById(attrGroupEntity);


        if (vo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()) {
            //??????????????????????????????????????????
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, entity);
            Integer integer = attrAttrgroupRelationDao.
                    selectCount(
                            new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", vo.getAttrId()));

            if (integer > 0) {
                attrAttrgroupRelationDao.update(entity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", entity.getAttrId()));

            } else {
                attrAttrgroupRelationDao.insert(entity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attr_group_id = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

//        List<AttrEntity> entities = new ArrayList<>();
//
//        for(int i = 0 ;i <attr_group_id.size();i++){
//            Long attrId = attr_group_id.get(i).getAttrId();
//
//            AttrEntity attrEntity = this.getById(attrId);
//            entities.add(attrEntity);
//        }
        //??????
        List<Long> collect = attr_group_id.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        if (collect == null || collect.size() <= 0) {
            return null;
        }

        List<AttrEntity> entities = this.listByIds(collect);

        return entities;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getOtherAttr(Map<String, Object> params, Long attrgroupId) {
        //1???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //1????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        //2?????????????????????????????????????????????????????????
        //2.1??????????????????????????????
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>()
                        .eq("catelog_id", attrGroupEntity.getCatelogId()));//????????????????????????
        //??????????????????id

        List<Long> collect = attrGroupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2???????????????????????????

        List<AttrAttrgroupRelationEntity> attr_group_id = attrAttrgroupRelationDao.
                selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));

        //???????????????????????????attr_id
        List<Long> collect1 = attr_group_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //2.3???????????????????????????????????????????????????

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type",ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode());
        if (collect1 != null && collect1.size() > 0){
            wrapper.notIn("attr_id", collect1);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("attr_id",key).or().like("attr_name",key);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);


        return new PageUtils(page);


    }
    //??????
    @Override
    public PageUtils getOtherAttr2(Map<String, Object> params, Long attrgroupId) {
        //?????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //????????????????????????????????????
        List<AttrEntity> attrEntities = this.baseMapper.selectList(new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId));

        //???????????????????????????

        List<Long> attr_id = attrEntities.stream().map((item) -> {
            AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao
                    .selectById(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", item.getAttrId()));
            if (entity == null)
                return item.getAttrId();
            return null;
        }).collect(Collectors.toList());

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("attr_id",key).or().like("attr_name",key);
        }
        IPage<AttrEntity> attr_id1 = this.page(new Query<AttrEntity>().getPage(params), wrapper.in("attr_id", attr_id));

        return new PageUtils(attr_id1);
    }

    @Override
    public void updateBySpuId(Long spuId, List<ProductAttrValueEntity> entities) {


        List<ProductAttrValueEntity> collect = entities.stream().map(a -> {
            ProductAttrValueEntity one = productAttrValueService.getOne(
                    new QueryWrapper<ProductAttrValueEntity>()
                            .eq("spu_id", spuId).eq("attr_id", a.getAttrId()));

            if (one != null){
                one.setAttrValue(a.getAttrValue());
                one.setQuickShow(a.getQuickShow());
            }

            return one;
        }).collect(Collectors.toList());

        productAttrValueService.updateBatchById(collect);

        //?????????spu????????????????????????????????? ???????????????????????????

    }

    @Override
    public List<AttrEntity> selectSearchAttrs(List<Long> collect1) {
        List<AttrEntity> attrEntities = this.listByIds(collect1);

        List<AttrEntity> collect = attrEntities.stream().filter(attrEntity -> {
            return attrEntity.getSearchType() == 1;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<Long> selectSearchAttrsIds(List<Long> attrids) {
        List<AttrEntity> attrEntities = this.listByIds(attrids);

        List<Long> collect = attrEntities.stream().filter(item -> {
            return item.getSearchType() == 1;
        }).map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());


        return collect;
    }


}