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
            // 如果是 base 就是基本属性 插入1 否则插入0
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
        // 先查询三级分类名字、分组名字 再封装
        List<AttrEntity> records = page.getRecords();
        // attrRespVos 就是最终封装好的Vo
        List<AttrRespVo> attrRespVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            // 1.设置分类和分组的名字  先获取中间表对象  给attrRespVo 封装分组名字
            if("base".equalsIgnoreCase(attrType)){
                // attr的关联关系 当它没有分组的时候就不保存了
                AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (entity != null && entity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity);
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            // 2.查询分类id 给attrRespVo 封装三级分类名字
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
        //保存属性
        AttrEntity attrEntity = new AttrEntity();

        BeanUtils.copyProperties(attrVo, attrEntity);

        this.save(attrEntity);
        //保存属性和属性分组关联关系
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

            //如果有分组的话
            if (entity != null) {
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
        BeanUtils.copyProperties(vo, attrEntity);
        this.updateById(attrEntity);

//        AttrGroupEntity attrGroupEntity = new AttrGroupEntity();
//        BeanUtils.copyProperties(vo,attrGroupEntity);
//        //更新属性分组信息
//        attrGroupDao.updateById(attrGroupEntity);


        if (vo.getAttrType() == ProductAttrTypeEnum.ATTR_TYPE_BASE.getCode()) {
            //更新属性和属性分组的关联关系
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
        //优化
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
     * 获取当前分组没有关联的所有属性
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getOtherAttr(Map<String, Object> params, Long attrgroupId) {
        //1当前分组只能关联当前分类里边的属性（比如说，主体分组，只能关联手机下边的一些属性）
        //1先查询当前分组所属的分类
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        //2当前分组只能关联别的分组没有引用的属性
        //2.1当前分类下的其他分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>()
                        .eq("catelog_id", attrGroupEntity.getCatelogId()));//当前分类下的属性
        //收集分组的组id

        List<Long> collect = attrGroupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2这些分组的关联属性

        List<AttrAttrgroupRelationEntity> attr_group_id = attrAttrgroupRelationDao.
                selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));

        //获取这些关联属性的attr_id
        List<Long> collect1 = attr_group_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //2.3从当前分类的所有属性中移除这些属性

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
    //失败
    @Override
    public PageUtils getOtherAttr2(Map<String, Object> params, Long attrgroupId) {
        //获取当前属性分组的分类
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //查出当前分类下的所有属性
        List<AttrEntity> attrEntities = this.baseMapper.selectList(new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId));

        //剔除掉有关联关系的

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

        //直接将spu对应的属性全部进行删除 然后在进行批量插入

    }


}