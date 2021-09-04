package com.jc.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.jc.gulimall.product.entity.AttrEntity;
import com.jc.gulimall.product.service.AttrService;
import com.jc.gulimall.product.service.CategoryService;
import com.jc.gulimall.product.vo.AttrRespVo;
import com.jc.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jc.gulimall.product.entity.AttrGroupEntity;
import com.jc.gulimall.product.service.AttrGroupService;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.R;



/**
 * 属性分组
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
//        params.put("catelog_id",catelogId);
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }

//    获取指定分组关联的所有属性
    /**
     * 列表
     */
    @RequestMapping("/{attrgroupId}/attr/relation")
    public R getRelation(@PathVariable("attrgroupId") Long attrgroupId){
//        params.put("catelog_id",catelogId);
//        PageUtils page = attrGroupService.queryPage(params);
         List<AttrEntity> entityList=attrService.getRelationAttr(attrgroupId);


        return R.ok().put("data", entityList);
    }



    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();

        Long[] catelogPath=categoryService.findCatelogPath(catelogId);

        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity entity){
		attrGroupService.save(entity);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    @RequestMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody AttrAttrgroupRelationEntity[] entitys){
        attrGroupService.relationDelete(entitys);

        return R.ok();
    }

}
