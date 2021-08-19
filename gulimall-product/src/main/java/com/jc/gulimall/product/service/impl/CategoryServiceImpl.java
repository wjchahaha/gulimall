package com.jc.gulimall.product.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.CategoryDao;
import com.jc.gulimall.product.entity.CategoryEntity;
import com.jc.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查找数据

        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //组装成父子的树形结构
        //1找到所有的一级分类
        List<CategoryEntity> level1menus = categoryEntities.stream().filter((item) ->{
             return  item.getParentCid() == 0;
        }).map((item) ->{
            item.setChildren(getChildrens(item,categoryEntities));
            return item;
        }).sorted((menu1,menu2)->{
          return  (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前产品在其他地方是否被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public int updateByIds(CategoryEntity[] categorys) {
        int count = 0;
        for(int i = 0;i<categorys.length;i++){
            count +=baseMapper.update(categorys[i],null);
        }

        return count;
    }


    private List<CategoryEntity> getChildrens(CategoryEntity target,List<CategoryEntity> categoryEntities){

        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> {
            return item.getParentCid().equals(target.getCatId());//递归出口是 找不到那个父id和targetCatId相同得了
        }).map(menu ->{//每一个菜单x及分类，依然会有子分类
            menu.setChildren(getChildrens(menu,categoryEntities));
            return menu;
        }).sorted((menu1,menu2)->
            (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());

        return collect;

    }

}