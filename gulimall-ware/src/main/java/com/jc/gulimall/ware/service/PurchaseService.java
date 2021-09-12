package com.jc.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.ware.entity.PurchaseEntity;
import com.jc.gulimall.ware.vo.DoneVo;
import com.jc.gulimall.ware.vo.MegroVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:34:53
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceiveList(Map<String, Object> params);

    void merge(MegroVo vo);


    void received(List<Long> ids);

    void done(DoneVo vo);

    void doneSuccess(DoneVo vo);
}

