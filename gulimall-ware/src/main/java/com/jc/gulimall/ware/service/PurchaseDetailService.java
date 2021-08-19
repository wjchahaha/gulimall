package com.jc.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:34:53
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

