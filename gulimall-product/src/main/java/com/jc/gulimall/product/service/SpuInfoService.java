package com.jc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.gulimall.product.entity.SpuInfoEntity;
import com.jc.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:31
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    PageUtils queryPageByCondation(Map<String, Object> params);

    void spuUp(Long spuId);
}

