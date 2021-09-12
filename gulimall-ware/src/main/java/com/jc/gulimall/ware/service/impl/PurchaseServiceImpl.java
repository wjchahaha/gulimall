package com.jc.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jc.common.constant.PruchaseConstant;
import com.jc.common.constant.PurchaseDetailConstant;
import com.jc.gulimall.ware.entity.PurchaseDetailEntity;
import com.jc.gulimall.ware.service.PurchaseDetailService;
import com.jc.gulimall.ware.service.WareSkuService;
import com.jc.gulimall.ware.vo.DoneVo;
import com.jc.gulimall.ware.vo.Item;
import com.jc.gulimall.ware.vo.MegroVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.ware.dao.PurchaseDao;
import com.jc.gulimall.ware.entity.PurchaseEntity;
import com.jc.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceiveList(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Override
    public void merge(MegroVo vo) {
        //采购需求集合 过滤出来所选中的采购需求必须都是新建状态
        List<Long> items = vo.getItems();
        List<PurchaseDetailEntity> collect = items.stream().map((item) -> {
            PurchaseDetailEntity entity = purchaseDetailService.getById(item);
            ;
//            entity.setPurchaseId(lastPurchaseId);
            return entity;
        }).filter(entity -> {
            //true的话不过滤  采购单状态只能是新建的才能分配
            return entity.getStatus() == PurchaseDetailConstant.CREATE.getCode();
        }).collect(Collectors.toList());
        if (collect != null && collect.size() > 0) {

            Long purchaseId = vo.getPurchaseId();

            if (purchaseId == null) {
                //如果没有选中合并哪个采购清单的话  新建一个采购清单
                PurchaseEntity purchaseEntity = new PurchaseEntity();

                purchaseEntity.setStatus(PruchaseConstant.CREATEED.getCode());
                purchaseEntity.setCreateTime(new Date());
                purchaseEntity.setUpdateTime(new Date());

                this.save(purchaseEntity);
                //
                purchaseId = purchaseEntity.getId();
            }
            Long lastPurchaseId = purchaseId;
            PurchaseEntity purchaseEntity = this.getById(lastPurchaseId);
            if (purchaseEntity.getStatus() == PruchaseConstant.CREATEED.getCode()
                    || purchaseEntity.getStatus() == PruchaseConstant.ALLOTED.getCode()) {

                //批量修改根据id
                if (collect != null && collect.size() > 0) {
                    List<PurchaseDetailEntity> collect1 = collect.stream().map(entity -> {
                        entity.setStatus(PurchaseDetailConstant.ALLOTED.getCode());
                        entity.setPurchaseId(lastPurchaseId);
                        return entity;
                    }).collect(Collectors.toList());

                    purchaseDetailService.updateBatchById(collect1);
                    //只有采购单状态是新建的时候才修改采购单状态
                    purchaseEntity.setUpdateTime(new Date());
                    purchaseEntity.setId(lastPurchaseId);
                    purchaseEntity.setStatus(PruchaseConstant.ALLOTED.getCode());
                    this.updateById(purchaseEntity);
                }

                //修改采购单的最后修改时间


            }
        }

    }

    @Override
    public void received(List<Long> ids) {

        //确认采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity entity = this.getById(id);
            return entity;
        }).filter(item -> {
            //false的话就过滤掉  既不是新建状态也不是已分配状态的话就过滤掉
            return (item.getStatus() == PruchaseConstant.CREATEED.getCode()
                    || item.getStatus() == PruchaseConstant.ALLOTED.getCode());
        }).collect(Collectors.toList());

        //修改采购单状态  collect 收集到的新建和已分配的采购单
        List<PurchaseEntity> collect1 = collect.stream().map(item -> {
            item.setStatus(PruchaseConstant.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            //2修改采购单中采购需求状态

            QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("purchase_id", item.getId());
            //2.1根据条件查询出采购单中的采购项
            List<PurchaseDetailEntity> list = purchaseDetailService.list(wrapper);
            if (list != null && list.size() > 0) {
                List<PurchaseDetailEntity> collect2 = list.stream().map(entity -> {
                    //将每个采购项状态更新
                    entity.setStatus(PurchaseDetailConstant.BUYING.getCode());
                    return entity;
                }).collect(Collectors.toList());
                //修改采购单中对应的采购项状态
                purchaseDetailService.updateBatchById(collect2);
            }
            return item;
        }).collect(Collectors.toList());

        this.updateBatchById(collect1);

    }

    /**
     * {
     * id: 123,//采购单id
     * items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     *
     * @param vo
     */
    @Override
    public void done(DoneVo vo) {
        //采购单id设置为已领取->已完成
        Long id = vo.getId();
        PurchaseEntity purchaseEntity = this.getById(id);
        if (purchaseEntity != null){
            purchaseEntity.setStatus(PruchaseConstant.FINISHED.getCode());
            this.updateById(purchaseEntity);
        }

        //根据items中的对象将采购需求设置为已完成
        List<Item> items = vo.getItems();
        //将状态不是3的过滤掉
        List<Item> collect = items.stream().filter(item -> {
            return item.getStatus() == PurchaseDetailConstant.FINISHED.getCode();
        }).collect(Collectors.toList());

        List<PurchaseDetailEntity> collect1 = collect.stream().map(item -> {
            PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
            entity.setStatus(PurchaseDetailConstant.FINISHED.getCode());
            return entity;
        }).collect(Collectors.toList());


        purchaseDetailService.updateBatchById(collect1);
    }

    @Transactional
    @Override
    public void doneSuccess(DoneVo vo) {
        //采购单状态  如果采购单中的采购需求有一个没玩采购完成则返回有异常
        Long id = vo.getId();
        //采购需求的状态
        List<Item> items = vo.getItems();
        List<PurchaseDetailEntity> detailEntities = new ArrayList<>();
        for(Item item:items){
            PurchaseDetailEntity entity = new PurchaseDetailEntity();

            if (item.getStatus() == PurchaseDetailConstant.FINISHED.getCode()){
                entity.setStatus(PurchaseDetailConstant.FINISHED.getCode());
                //将采购成功的进行入库
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());
            }else {
                entity.setStatus(PurchaseDetailConstant.BUYERROR.getCode());
            }
            entity.setId(item.getItemId());
            detailEntities.add(entity);

        }
        purchaseDetailService.updateBatchById(detailEntities);

        PurchaseEntity byId = this.getById(id);

        if (detailEntities.size() < items.size()){//如果收集到的小于items中的
            byId.setStatus(PruchaseConstant.ERROR.getCode());

        }else{

            byId.setStatus(PruchaseConstant.FINISHED.getCode());


        }
        byId.setUpdateTime(new Date());
        this.updateById(byId);
    }


}