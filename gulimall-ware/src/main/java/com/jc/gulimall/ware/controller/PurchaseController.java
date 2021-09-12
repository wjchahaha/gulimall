package com.jc.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jc.gulimall.ware.vo.DoneVo;
import com.jc.gulimall.ware.vo.MegroVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jc.gulimall.ware.entity.PurchaseEntity;
import com.jc.gulimall.ware.service.PurchaseService;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.R;



/**
 * 采购信息
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:34:53
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * {
     *    id: 123,//采购单id
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     * @param ids
     * @return
     */
    @PostMapping("/done")
    public R done(@RequestBody DoneVo vo){
        purchaseService.done(vo);

        purchaseService.doneSuccess(vo);

        return R.ok();
    }
    /**
     *
     * @param
     * @return
     */
    @PostMapping("/received")
    public R received(@RequestBody  List<Long> ids){
        purchaseService.received(ids);

        return R.ok();
    }
    /**
     * ware/purchase/merge
     * {
     *   purchaseId: 1, //整单id
     *   items:[1,2,3,4] //合并项集合
     * }
     * @return
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MegroVo vo){
        purchaseService.merge(vo);

        return R.ok();
    }
    ///ware/purchase/unreceive/list
    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceiveList(params);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
