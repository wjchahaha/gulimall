package com.jc.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jc.common.exception.BizCodeEnume;
import com.jc.gulimall.ware.exception.NoStockException;
import com.jc.gulimall.ware.vo.LockStockRes;
import com.jc.gulimall.ware.vo.SkuHasStockVo;
import com.jc.gulimall.ware.vo.WareSkuLockVo;
import org.apache.tomcat.jni.BIOCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.jc.gulimall.ware.entity.WareSkuEntity;
import com.jc.gulimall.ware.service.WareSkuService;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.R;


/**
 * 商品库存
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:34:53
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    @Transactional
    @PostMapping("/order/lockStock")
    public R lockStock(@RequestBody WareSkuLockVo vo){
        try {
            boolean res = wareSkuService.lockStock(vo);
            return R.ok();
        }catch (NoStockException e){
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }

    }
    //查询sku是否有库存
    @PostMapping("/hasStock")
    public R hasStockBySkuIds(@RequestBody List<Long> skuIds) {

        List<SkuHasStockVo> skuHasStockVoList=wareSkuService.hasStock(skuIds);

        return R.ok().setData(skuHasStockVoList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
