package com.jc.gulimall.coupon.service.impl;

import com.jc.common.to.MemberPrice;
import com.jc.common.to.SkuReductionTo;
import com.jc.gulimall.coupon.entity.MemberPriceEntity;
import com.jc.gulimall.coupon.entity.SkuLadderEntity;
import com.jc.gulimall.coupon.service.MemberPriceService;
import com.jc.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.coupon.dao.SkuFullReductionDao;
import com.jc.gulimall.coupon.entity.SkuFullReductionEntity;
import com.jc.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Autowired
    private SkuFullReductionService skuFullReductionService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo to) {
        //5.4）保存sku的优惠；gulimall-sms->`sms_sku_ladder(打折表几件打几折)`
        if (to.getFullCount() > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            BeanUtils.copyProperties(to, skuLadderEntity);
            skuLadderService.save(skuLadderEntity);
        }
//        skuLadderEntity.setPrice(); 下订单的时候计算就可
          // `sms_sku_full_reduction`(满减信息)`sms_member_price`（会员价格)
        //保存sku及满减信息
        if (to.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(to,fullReductionEntity);
            skuFullReductionService.save(fullReductionEntity);
        }

        //保存sms_member_price`（会员价格)

        List<MemberPrice> memberPrices = to.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(memberPrice -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(to.getSkuId());
            memberPriceEntity.setMemberLevelId(memberPrice.getId());
            memberPriceEntity.setMemberLevelName(memberPrice.getName());
            memberPriceEntity.setMemberPrice(memberPrice.getPrice());
            memberPriceEntity.setAddOther(1);

            return memberPriceEntity;
        }).filter((memberPriceEntity)->{
            //false 不过滤
            return memberPriceEntity.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);

    }

}