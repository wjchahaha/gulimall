package com.jc.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.gulimall.product.entity.BrandEntity;
import com.jc.gulimall.product.vo.BrandResVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.jc.gulimall.product.service.CategoryBrandRelationService;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

//    @Autowired
//    private BrandService brandService;
//
//    @Autowired
//    private CategoryService categoryService;

    /**
     * 获取分类关联的品牌
     *
     */
    @GetMapping("/brands/list")
    public R brandsList(@RequestParam("catId") Long catId){

        List<BrandEntity> data=categoryBrandRelationService.selectBrandsByCatId(catId);

        List<BrandResVo> collect = data.stream().map((item) -> {
            BrandResVo vo = new BrandResVo();
            vo.setBrandId(item.getBrandId());
            vo.setBrandName(item.getName());
            return vo;
        }).collect(Collectors.toList());

        return R.ok().put("data",collect);
    }
    /**
     * 获取当前品牌关联的所有分类列表
     * @param brandId
     * @return
     */
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId)
        );

        return R.ok().put("data", data);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){


        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
