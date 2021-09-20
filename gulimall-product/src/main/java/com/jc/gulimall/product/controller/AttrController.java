package com.jc.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jc.gulimall.product.entity.ProductAttrValueEntity;
import com.jc.gulimall.product.service.ProductAttrValueService;
import com.jc.gulimall.product.vo.AttrRespVo;
import com.jc.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jc.gulimall.product.entity.AttrEntity;
import com.jc.gulimall.product.service.AttrService;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.R;





/**
 * 商品属性
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;
    /**
     *
     * product/attr/base/listforspu/{spuId}
     */
    @RequestMapping("/base/listforspu/{spuId}")
    public R listforspu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> list = productAttrValueService.listforspu(spuId);

        return R.ok().put("data", list);
    }
    /**
     * 列表
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId,
                  @PathVariable("attrType") String attrType){
        PageUtils page = attrService.queryPage(params,catelogId,attrType);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo attr = attrService.selectAttrRespVo(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttrVo(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrRespVo attrRespVo){
		attrService.updateDetail(attrRespVo);

        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    public R updateBySpuId(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        attrService.updateBySpuId(spuId,entities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
