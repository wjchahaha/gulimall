package com.jc.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jc.common.constant.ProductAttrTypeEnum;
import com.jc.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.jc.gulimall.product.dao.AttrGroupDao;
import com.jc.gulimall.product.dao.CategoryDao;
import com.jc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.jc.gulimall.product.entity.AttrGroupEntity;
import com.jc.gulimall.product.entity.CategoryEntity;
import com.jc.gulimall.product.service.CategoryService;
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
import com.jc.gulimall.product.entity.AttrEntity;
import com.jc.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType) {
        String key = (String) params.get("key");

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>();
        //如果不是base的话 就代表是销售属性
        wrapper.eq("attr_type","base".equalsIgnoreCase(attrType)
                ? ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode() : ProductAttrTypeEnum.ATTR_TYPE_SALEm.getCode());
        if (!StringUtils.isEmpty(key)) {
            //构造查询条件
            wrapper.and((obj) -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                    wrapper);
            PageUtils pageUtils = new PageUtils(page);

            List<AttrEntity> records = page.getRecords();
            List<AttrRespVo> collect = records.stream().map((item) -> {

                AttrRespVo attrRespVo = new AttrRespVo();
                BeanUtils.copyProperties(item,attrRespVo);


                CategoryEntity categoryEntity = categoryDao.selectById(item.getCatelogId());
                //根据cateLogId查询 catelogName
                if (categoryEntity != null) {
                    attrRespVo.setCatelogName(categoryEntity.getName());
                }

                if(attrRespVo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()){
                    //根据attrId查询attr_Gruop_id在查询groupName
                    AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.
                            selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrRespVo.getAttrId()));
                    if (entity != null) {
                        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity.getAttrGroupId());
                        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }

                return attrRespVo;
            }).collect(Collectors.toList());
            pageUtils.setList(collect);

            return pageUtils;
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                    wrapper);

            PageUtils pageUtils = new PageUtils(page);

            List<AttrEntity> records = page.getRecords();
            List<AttrRespVo> collect = records.stream().map((item) -> {
                AttrRespVo attrRespVo = new AttrRespVo();
                //根据cateLogId查询 catelogName
                if (item.getCatelogId() != null) {
                    CategoryEntity categoryEntity = categoryDao.selectById(item.getCatelogId());
                    attrRespVo.setCatelogName(categoryEntity.getName());
                }

                //根据attrId查询attr_Gruop_id在查询groupName
                if(attrRespVo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()) {
                    if (item.getAttrId() != null) {
                        AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.selectById(item.getAttrId());
                        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity.getAttrGroupId());
                        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }

                return attrRespVo;
            }).collect(Collectors.toList());
            pageUtils.setList(collect);

            return pageUtils;
        }


    }

    @Override
    public void saveAttrVo(AttrVo attrVo) {
        //保存属性
        AttrEntity attrEntity = new AttrEntity();

        BeanUtils.copyProperties(attrVo, attrEntity);

        this.save(attrEntity);

        if(attrVo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()){
            //保存属性和属性分组关联关系

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

        BeanUtils.copyProperties(attrEntity,attrRespVo);
        if(attrEntity.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.selectById(attrId);

            //如果有分组的话
            if (entity != null){
                //为 vo填充分组id
                attrRespVo.setAttrGroupId(entity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity.getAttrGroupId());
                //设置分组名字
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        //填充path
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
        BeanUtils.copyProperties(vo,attrEntity);
        this.updateById(attrEntity);

//        AttrGroupEntity attrGroupEntity = new AttrGroupEntity();
//        BeanUtils.copyProperties(vo,attrGroupEntity);
//        //更新属性分组信息
//        attrGroupDao.updateById(attrGroupEntity);


        if(vo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()){
            //更新属性和属性分组的关联关系
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo,entity);
            Integer integer = attrAttrgroupRelationDao.
                    selectCount(
                            new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", vo.getAttrId()));

            if(integer > 0){
                attrAttrgroupRelationDao.update(entity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",entity.getAttrId()));

            }else{
                attrAttrgroupRelationDao.insert(entity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attr_group_id = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<AttrEntity> entities = new ArrayList<>();

        for(int i = 0 ;i <attr_group_id.size();i++){
            Long attrId = attr_group_id.get(i).getAttrId();

            AttrEntity attrEntity = this.getById(attrId);
            entities.add(attrEntity);
        }
        return entities;
    }


}